package microbat.handler;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import debuginfo.DebugInfo;
import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.DebugPilot;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.propagation.spp.StepExplaination;
import microbat.debugpilot.settings.DebugPilotSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.views.MicroBatViews;
import microbat.views.PathView;
import microbat.views.TraceView;

public class DebugPilotHandler extends AbstractHandler {
	
	protected TraceView buggyView;
	protected PathView pathView;
	
	protected Stack<NodeFeedbacksPair> userFeedbackRecords = new Stack<>();
	
	protected final static String DEBUGPILOT_STOP_MESSAGE = "DebugPilot stop";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("DebugPilot") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setup();
				execute();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}
	
	protected void execute() {
		Log.printMsg(getClass(), "");
		Log.printMsg(getClass(), "---------------------------");
		Log.printMsg(getClass(), "\t Start Debug Pilot");
		Log.printMsg(getClass(), "");
		
		// Precheck
		final Trace buggyTrace = this.buggyView.getTrace();
		if (buggyTrace == null) {
			Log.printMsg(getClass(), "Please setup the trace before propagation");
			return;
		}
		
		if (!this.isOuputReady()) {
			Log.printMsg(getClass(), "Please provide output to start");
			return;
		}
		
		DebugPilotSettings settings = new DebugPilotSettings();
		settings.setPropagatorSettings(PreferenceParser.getPreferencePropagatorSettings());
		settings.setPathFinderSettings(PreferenceParser.getPreferencePathFinderSettings());
		
		List<VarValue> outputs = DebugInfo.getOutputs();
		settings.setWrongVars(new HashSet<>(outputs));
		
		List<VarValue> inputs = DebugInfo.getInputs();
		settings.setCorrectVars(new HashSet<>(inputs));
		settings.setTrace(buggyTrace);
		
		final TraceNode outputNode = outputs.get(0).isConditionResult() ? DebugInfo.getNodeFeedbackPair().getNode() : this.getOutputNode(outputs.get(0));
		settings.setOutputNode(outputNode);
		
		if (outputNode.isReadVariablesContains(outputs.get(0).getVarID())) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(outputs.get(0), null));
			userFeedbackRecords.add(new NodeFeedbacksPair(outputNode, feedback));
		} else {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
			userFeedbackRecords.add(new NodeFeedbacksPair(outputNode, feedback));
		}
		
		// Initialize DebugPilot
		final DebugPilot debugPilot = new DebugPilot(settings);
		
		boolean isEnd = false;
		while (!DebugInfo.isRootCauseFound() && !DebugInfo.isStop() && !isEnd) {
			Log.printMsg(getClass(), "---------------------------");
			// Update feedback
			debugPilot.updateFeedbacks(this.userFeedbackRecords);
			this.userFeedbackRecords.clear();
			
			// Multi-Slicing 
			debugPilot.multiSlicing();
			
			// Probability Probability
			Log.printMsg(this.getClass(), "Propagating probability ...");
			long startTime = System.currentTimeMillis();
			debugPilot.propagate();
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime) / 1000;
			Log.printMsg(this.getClass(), "Propagation Duration: " + duration + " s");
			
			Log.printMsg(this.getClass(), "Locating root cause ...");
			TraceNode rootCause = debugPilot.locateRootCause();
			
			Log.printMsg(this.getClass(), "Constructing path to root cause ...");
			final FeedbackPath proposedPath =  debugPilot.constructPath(rootCause);
			
			// Update path view
			this.pathView.setActionPath(proposedPath);
			this.updateView();
			Log.printMsg(getClass(), "Please give you feedback on path view");
			
			// Ensure user give feedback on path
			NodeFeedbacksPair correctingFeedback = null;
			while (correctingFeedback == null) {
				correctingFeedback = this.waitForFeedback();
				if (correctingFeedback == null) {
					Log.printMsg(this.getClass(), DebugPilotHandler.DEBUGPILOT_STOP_MESSAGE);
					return;
				}
				final TraceNode userNode = correctingFeedback.getNode();
				if (!proposedPath.contains(userNode)) {
					correctingFeedback = null;
					Log.printMsg(this.getClass(), "Please give feedback only on the path node but node " + userNode.getOrder() + " is given");
				}
			}
			
			// Update feedback accordingly
			int pathIdx = 0;
			while (pathIdx < proposedPath.getLength()) {
				final NodeFeedbacksPair proposedPair = proposedPath.get(pathIdx);
				final TraceNode proposedNode = proposedPair.getNode();
				final TraceNode userNode = correctingFeedback.getNode();
				if (!proposedNode.equals(userNode)) {
					this.userFeedbackRecords.add(proposedPair);
					pathIdx += 1;
				} else {
					/*
					 * Handle three cases correspondingly
					 * 1. CORRECT
					 * 		Omission bug occur
					 * 2. Invalid feedback (cannot find next node in trace based on give feedback)
					 * 		Omission bug
					 * 3. Valid feedback
					 * 		Record it in the stack
					 */
					if (correctingFeedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
						NodeFeedbacksPair previousFeedbacksPair = this.userFeedbackRecords.peek();
						proposedPath.replacePair(correctingFeedback);
						this.handleOmissionBug(userNode, previousFeedbacksPair.getNode(), previousFeedbacksPair.getFirstFeedback(), proposedPath);
						isEnd = true;
						break;
					} else if (!this.isValidFeedback(correctingFeedback)) {
						Log.printMsg(this.getClass(), "Cannot find next node. Omission bug detected");
						NodeFeedbacksPair previousFeedbacksPair = this.userFeedbackRecords.peek();
						this.handleOmissionBug(userNode, previousFeedbacksPair.getNode(), previousFeedbacksPair.getFirstFeedback(), proposedPath);
						isEnd = true;
						break;
					} else { 
						Log.printMsg(this.getClass(), "Wong feedback on path: " + pathIdx + " node: " + userNode.getOrder() +  " , start propagation again");
						this.userFeedbackRecords.add(correctingFeedback);
						break;
					}
				}
			}
		}
	}
	
	protected void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				buggyView = MicroBatViews.getTraceView();
				pathView = MicroBatViews.getPathView();
				pathView.setBuggyView(buggyView);
			}
		});
	}
	
	protected boolean isOuputReady() {
		return !DebugInfo.getOutputs().isEmpty();
	}
	
	protected TraceNode getOutputNode(final VarValue outputVar) {
		final Trace trace = this.buggyView.getTrace();
		for (int order = trace.size(); order>=0; order--) {
			TraceNode node = trace.getTraceNode(order);
			final String varID = outputVar.getVarID();
			if (node.isReadVariablesContains(varID) || node.isWrittenVariablesContains(varID)) {
				return node;
			}
		}
		return null;
	}
	
	protected void updateView() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				buggyView.updateData();					
				pathView.updateData();					
			}
		});
	}
	
	protected NodeFeedbacksPair waitForFeedback() {
		DebugInfo.waitForFeedbackOrRootCauseOrStop();
		NodeFeedbacksPair userPairs = DebugInfo.getNodeFeedbackPair();
		DebugInfo.clearNodeFeedbackPairs();
		return userPairs;
	}
	
	protected boolean isValidFeedback(final NodeFeedbacksPair pair) {
		for (UserFeedback feedback : pair.getFeedbacks()) {
			TraceNode nextNode = TraceUtil.findNextNode(pair.getNode(), feedback, this.buggyView.getTrace());
			if (nextNode == null) {
				return false;
			}
		}
		return true;
	}
	
	protected void handleOmissionBug(final TraceNode startNode, final TraceNode endNode, final UserFeedback userFeedback, final FeedbackPath feedbackPath) {
		Objects.requireNonNull(startNode, Log.genMsg(this.getClass(), "Start node cannot be null"));
		Objects.requireNonNull(endNode, Log.genMsg(this.getClass(), "End node cannot be null"));
		Objects.requireNonNull(userFeedback, Log.genMsg(this.getClass(), "User feedback cannot be null"));
		Objects.requireNonNull(feedbackPath, Log.genMsg(this.getClass(), "Feedback path cannot be null"));
		
		feedbackPath.removePathBeforeNode(startNode);
		this.updateOmissionNodeReason(startNode, endNode, userFeedback);
		
		System.out.println("Omission bug detected ...");
		final int startOrder = startNode.getOrder();
		final int endOrder = endNode.getOrder();
		List<TraceNode> candidateNodes = this.buggyView.getTrace().getExecutionList().stream().filter(node -> node.getOrder() > startOrder && node.getOrder() < endOrder).toList();
		
		if (candidateNodes.isEmpty()) {
			this.reportOmissionBug(startNode, endNode, userFeedback);
		} else if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			this.handleControlOmissionBug(candidateNodes, feedbackPath);
		} else if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			this.handleDataOmissionBug(candidateNodes, userFeedback.getOption().getReadVar(), feedbackPath);
		} else {
			throw new IllegalArgumentException(Log.genMsg(getClass(), "Unhandled feedback type: " + userFeedback));
		}
	}
	
	protected void handleControlOmissionBug(final List<TraceNode> rootCauseCandidates, final FeedbackPath feedbackPath) {
		List<TraceNode> sortedList = this.sortNodeList(rootCauseCandidates);
		int left = 0;
		int right = sortedList.size();
		while (left <= right) {
			int mid = left + (right - left) / 2;
			final TraceNode node = sortedList.get(mid);
			node.reason = StepExplaination.BINARY_SEARCH;
			feedbackPath.addPairByOrder(node, new UserFeedback(UserFeedback.ROOTCAUSE));
			this.pathView.setActionPath(feedbackPath);
			this.updateView();
			System.out.println("Proposed root cause is node: " + node.getOrder() + ". If you does not agree, please give feedback on this node.");
			NodeFeedbacksPair pair = this.waitForFeedback();
			if (pair == null) {
				Log.printMsg(this.getClass(), DebugPilotHandler.DEBUGPILOT_STOP_MESSAGE);
				return;
			}
			
			if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
				left = mid+1;
			} else {
				right = mid-1;
			}
			node.reason = StepExplaination.USRE_CONFIRMED;
			feedbackPath.replacePair(pair);
			this.pathView.setActionPath(feedbackPath);
			this.updateView();
		}
		System.out.println("No more trace node can be recommended");
	}
	
	protected void handleDataOmissionBug(final List<TraceNode> rootCauseCandidates, final VarValue wrongVar, final FeedbackPath feedbackPath) {
		List<TraceNode> sortedCandidateList = this.sortNodeList(rootCauseCandidates);
		List<TraceNode> relatedCandidateList = sortedCandidateList.stream().filter(node -> node.isReadVariablesContains(wrongVar.getVarID())).toList();
		int startOrder = sortedCandidateList.get(0).getOrder();
		int endOrder = sortedCandidateList.get(sortedCandidateList.size()-1).getOrder();
		for (TraceNode node : relatedCandidateList) {
			node.reason = StepExplaination.RELATED;
			feedbackPath.addPairByOrder(node, new UserFeedback(UserFeedback.UNCLEAR));
			this.pathView.setActionPath(feedbackPath);
			this.updateView();
			System.out.println("Please give a feedback on node: " + node.getOrder());
			NodeFeedbacksPair pair = this.waitForFeedback();
			if (pair == null) {
				Log.printMsg(this.getClass(), DebugPilotHandler.DEBUGPILOT_STOP_MESSAGE);
				return;
			}
			feedbackPath.replacePair(pair);
			if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
				startOrder = pair.getNode().getOrder();
			} else {
				endOrder = pair.getNode().getOrder();
				break;
			}
		}
		
		final int startOrder_ = startOrder;
		final int endOrder_ = endOrder;
		List<TraceNode> filteredCandidates = sortedCandidateList.stream().filter(node -> node.getOrder() >= startOrder_ && node.getOrder() <= endOrder_).toList();
		for (TraceNode node : filteredCandidates) {
			System.out.println("Proposed first deviation node: " + node.getOrder() + ". Please give feedback if you do not agree");
			node.reason = StepExplaination.SCANNING;
			feedbackPath.addPairByOrder(node, new UserFeedback(UserFeedback.ROOTCAUSE));
			this.pathView.setActionPath(feedbackPath);
			this.updateView();
			@SuppressWarnings("unused")
			NodeFeedbacksPair pair = this.waitForFeedback();
			if (pair == null) {
				Log.printMsg(this.getClass(), DebugPilotHandler.DEBUGPILOT_STOP_MESSAGE);
				return;
			}
			node.reason = StepExplaination.USRE_CONFIRMED;
			feedbackPath.replacePair(pair);
			this.pathView.setActionPath(feedbackPath);
			this.updateView();
		}
		System.out.println("No more trace node can be recommeded");
	}
	
	protected List<TraceNode> sortNodeList(final List<TraceNode> list) {
		return list.stream().sorted(
				new Comparator<TraceNode>() {
					@Override
					public int compare(TraceNode node1, TraceNode node2) {
						return node1.getOrder() - node2.getOrder();
					}
				}
			).toList();
	}
	
	public void updateOmissionNodeReason(final TraceNode startNode, final TraceNode endNode, final UserFeedback feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			startNode.reason = StepExplaination.MISS_BRANCH;
			endNode.reason = StepExplaination.MISS_BRANCH;
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)){
			final VarValue wrongVar = feedback.getOption().getReadVar();
			startNode.reason = StepExplaination.MISS_DEF(wrongVar.getVarName());
			endNode.reason = StepExplaination.MISS_DEF(wrongVar.getVarName());
		} else {
			throw new IllegalArgumentException(Log.genMsg(getClass(), "Unhandled feedback type: " + feedback));
		}
	}
	
	protected void reportOmissionBug(final TraceNode startNode, final TraceNode endNode, final UserFeedback feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			this.reportMissingBranchOmissionBug(startNode, endNode);
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			final VarValue wrongVar = feedback.getOption().getReadVar();
			this.reportMissingAssignmentOmissionBug(startNode, endNode, wrongVar);
		} else {
			throw new IllegalArgumentException(Log.genMsg(getClass(), "Unhandled feedback type: " + feedback));
		}
	}
	
	protected void reportMissingBranchOmissionBug(final TraceNode startNode, final TraceNode endNode) {
		Log.printMsg(this.getClass(), "-------------------------------------------");
		Log.printMsg(this.getClass(), "Omission bug detected");
		Log.printMsg(this.getClass(), "Scope begin: " + startNode.getOrder());
		Log.printMsg(this.getClass(), "Scope end: " + endNode.getOrder());
		Log.printMsg(this.getClass(), "Omission Type: Missing Branch");
		Log.printMsg(this.getClass(), "-------------------------------------------");
	}
	
	protected void reportMissingAssignmentOmissionBug(final TraceNode startNode, final TraceNode endNode, final VarValue var) {
		Log.printMsg(this.getClass(), "-------------------------------------------");
		Log.printMsg(this.getClass(), "Omission bug detected");
		Log.printMsg(this.getClass(), "Scope begin: " + startNode.getOrder());
		Log.printMsg(this.getClass(), "Scope end: " + endNode.getOrder());
		Log.printMsg(this.getClass(), "Omission Type: Missing Assignment of " + var.getVarName());
		Log.printMsg(this.getClass(), "-------------------------------------------");
	}

}
