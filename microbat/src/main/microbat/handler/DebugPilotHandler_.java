package microbat.handler;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.about.AboutUtils;

import microbat.debugpilot.DebugPilotInfo;
import microbat.debugpilot.DebugPilot;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.fsc.AbstractDebugPilotState;
import microbat.debugpilot.fsc.DebugPilotState;
import microbat.debugpilot.fsc.DebugPilotFiniteStateMachine;
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
import microbat.views.DebugFeedbackView;
import microbat.views.DebugPilotFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.PathView;
import microbat.views.TraceView;

public class DebugPilotHandler_ extends AbstractHandler {
	
	protected TraceView buggyView;
	protected PathView pathView;
	
	protected Stack<NodeFeedbacksPair> userFeedbackRecords = new Stack<>();
	protected DebugPilotInfo info = DebugPilotInfo.getInstance();
	
	protected final static String DEBUGPILOT_STOP_MESSAGE = "DebugPilot stop";
	
	protected FeedbackPath proposedPath = null;
//	
	protected boolean isEnd = false;
	
	public DebugPilotHandler_() {

	}
	
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

		if (!this.isDebugPilotReady()) {
			return;
		}
		
		Log.printMsg(getClass(), "");
		Log.printMsg(getClass(), "---------------------------");
		Log.printMsg(getClass(), "\t Start Debug Pilot");
		Log.printMsg(getClass(), "");
		
		DebugPilotSettings settings = new DebugPilotSettings();
		settings.setPropagatorSettings(PreferenceParser.getPreferencePropagatorSettings());
		settings.setPathFinderSettings(PreferenceParser.getPreferencePathFinderSettings());
		
		TraceNode outputNode = DebugPilotInfo.getInstance().getOutputNode();
		settings.setOutputNode(outputNode);
		
		VarValue wrongVar = DebugPilotInfo.getInstance().getOutputs().get(0);
		Set<VarValue> wrongVarSet = new HashSet<>();
		wrongVarSet.add(wrongVar);
		settings.setWrongVars(wrongVarSet);

		settings.setTrace(this.buggyView.getTrace());
		
		if (outputNode.isReadVariablesContains(wrongVar.getVarID())) {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(wrongVar, null));
			userFeedbackRecords.add(new NodeFeedbacksPair(outputNode, feedback));
		} else {
			UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
			userFeedbackRecords.add(new NodeFeedbacksPair(outputNode, feedback));
		}
		
		// Initialize DebugPilot
		final DebugPilot debugPilot = new DebugPilot(settings);
		
		isEnd = false;
		while (!DebugPilotInfo.getInstance().isStop() && !isEnd) {
			Log.printMsg(getClass(), "---------------------------");
			
			// Update feedback
			debugPilot.updateFeedbacks(this.userFeedbackRecords);
			this.userFeedbackRecords.clear();
			
			// Multi-Slicing         
			debugPilot.multiSlicing();
			
			// Probability Probability
			debugPilot.propagate();
			
			TraceNode rootCause = debugPilot.locateRootCause();
			
			this.proposedPath =  debugPilot.constructPath(rootCause);
			
			// Update path view
			this.updatePathView(proposedPath);

			// Ensure user give feedback on path
			NodeFeedbacksPair correctingFeedback = this.waitForFeedback();
			if (correctingFeedback == null) {
				return;
			}
			
			// Handle the case that root cause is reach
			if (correctingFeedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
				proposedPath.replacePair(correctingFeedback);
				proposedPath.removePathAfterNode(correctingFeedback.getNode());
				this.updatePathView(proposedPath);
				return;
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
//						isEnd = true;
						break;
					} else if (!this.canLeadToNextNode(correctingFeedback)) {
						Log.printMsg(this.getClass(), "Cannot find next node. Omission bug detected");
						NodeFeedbacksPair previousFeedbacksPair = this.userFeedbackRecords.peek();
						this.handleOmissionBug(userNode, previousFeedbacksPair.getNode(), previousFeedbacksPair.getFirstFeedback(), proposedPath);
//						isEnd = true;
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
		return !DebugPilotInfo.getInstance().getOutputs().isEmpty();
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
	
	protected boolean isDebugPilotReady() {
		/*
		 * DebugPilot is ready when:
		 * 
		 * 1. Trace is generated
		 * 2. DebugPilot info is ready
		 * 
		 * Info is ready when user indicate either wrong variable or output node is in wrong branch
		 * 
		 * If output node is in wrong branch, then the wrong variable is be empty
		 * otherwise, there must be at least one wrong variable
		 */
		final Trace buggyTrace = this.buggyView.getTrace();
		if (buggyTrace == null) {
			this.popErrorDialog("Trace is not generated.");
			return false;
		}
		
		TraceNode outputNode = this.info.getOutputNode();
		if (outputNode == null) {
			this.popErrorDialog("Output node is empty. You can select the output node by selecting it in the trace view.");
			return false;
		}
		
		if (info.isOutputNodeWrongBranch()) {
			if (!this.info.getOutputs().isEmpty()) {
				this.popErrorDialog("When the output node should not be executed, there should not be wrong variables. Please double click the variables to erase them");
				return false;
			}
		} else {
			if (this.info.getOutputs().isEmpty()) {
				this.popErrorDialog("Please select one wrong variable to start DebugPilot. If the output node should not be executed, then please click the wrong branch button");
				return false;
			}
		}
		
		return true;
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
		final DebugPilotInfo info = DebugPilotInfo.getInstance();
		boolean isValidFeedback = false;
		NodeFeedbacksPair userPair = null;
		while (!isValidFeedback) {
			info.waifForFeedbacksPairOrStop();
			userPair = info.getNodeFeedbackPair();
			info.clearNodeFeedbackPairs();
			
			if (userPair == null) {
				// Null is also a valid feedback indicating stop of progress
				break;
			}
			isValidFeedback = this.checkIsValidFeedback(userPair);
		}
		return userPair;
	}
	
	protected boolean checkIsValidFeedback(final NodeFeedbacksPair pair) {
		// Feedback is invalid when there are conflicting feedback
		Objects.requireNonNull(pair, Log.genMsg(getClass(), "Given feedbacksPair cannot not null"));
		if (pair.getFeedbacks().size() == 1) {
			return true;
		}
		
		String feedbackType = null;
		for (UserFeedback feedback : pair.getFeedbacks()) {
			if (feedbackType == null) {
				feedbackType = feedback.getFeedbackType();
			} else if (!feedbackType.equals(feedback.getFeedbackType())) {
				this.popErrorDialog("You give conflicting feedback on node " + pair.getNode().getOrder());
				return false;
			}
		}
		
		if (!this.proposedPath.contains(pair.getNode())) {
			this.popErrorDialog("Please give the feedback on node that inside the path. Node: " + pair.getNode().getOrder() + " is not lie in path.");
			return false;
		}
		
		return true;
	}
	
	protected boolean canLeadToNextNode(final NodeFeedbacksPair pair) {
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
		
		feedbackPath.removePathAfterNode(startNode);
		this.updateOmissionNodeReason(startNode, endNode, userFeedback);
		this.updatePathView(feedbackPath);
		
		System.out.println("Omission bug detected ...");
		final int startOrder = startNode.getOrder();
		final int endOrder = endNode.getOrder();
		List<TraceNode> candidateNodes = this.buggyView.getTrace().getExecutionList().stream().filter(node -> node.getOrder() > startOrder && node.getOrder() < endOrder).toList();
		
		if (candidateNodes.isEmpty()) {
			this.reportOmissionBug(startNode, endNode, userFeedback);
			NodeFeedbacksPair feedback = this.waitForFeedback();
			if (feedback == null) {
				this.isEnd = true;
				return;
			}
			if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
				this.isEnd = true;
				return;
			}
			this.userFeedbackRecords.add(feedback);
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
				Log.printMsg(this.getClass(), DebugPilotHandler_.DEBUGPILOT_STOP_MESSAGE);
				this.isEnd = true;
				return;
			}
			if (pair.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
				this.isEnd = true;
				return;
			}
			
			if (rootCauseCandidates.contains(pair.getNode())) {
				this.userFeedbackRecords.add(pair);
				return;
			}
			if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
				left = mid+1;
			} else {
				right = mid-1;
			}
			node.reason = StepExplaination.USRE_CONFIRMED;
			feedbackPath.replacePair(pair);
			this.updatePathView(feedbackPath);
		}
		System.out.println("No more trace node can be recommended");
	}
	
	protected void updatePathView(final FeedbackPath path) {
		this.pathView.setActionPath(path);
		this.updateView();
	}
	
	protected void handleDataOmissionBug(final List<TraceNode> rootCauseCandidates, final VarValue wrongVar, final FeedbackPath feedbackPath) {
		List<TraceNode> sortedCandidateList = this.sortNodeList(rootCauseCandidates);
		List<TraceNode> relatedCandidateList = sortedCandidateList.stream().filter(node -> node.isReadVariablesContains(wrongVar.getVarID())).toList();
		int startOrder = sortedCandidateList.get(0).getOrder();
		int endOrder = sortedCandidateList.get(sortedCandidateList.size()-1).getOrder();
		for (TraceNode node : relatedCandidateList) {
			node.reason = StepExplaination.RELATED;
			feedbackPath.addPairByOrder(node, new UserFeedback(UserFeedback.UNCLEAR));
			this.updatePathView(feedbackPath);
			System.out.println("Please give a feedback on node: " + node.getOrder());
			NodeFeedbacksPair pair = this.waitForFeedback();
			if (pair == null) {
				Log.printMsg(this.getClass(), DebugPilotHandler_.DEBUGPILOT_STOP_MESSAGE);
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
				Log.printMsg(this.getClass(), DebugPilotHandler_.DEBUGPILOT_STOP_MESSAGE);
				return;
			}
			node.reason = StepExplaination.USRE_CONFIRMED;
			feedbackPath.replacePair(pair);
			this.updatePathView(feedbackPath);
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
	
	protected void popErrorDialog(final String errorMsg) {
		Display.getDefault().asyncExec(() -> {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell, "DebugPilot Error", errorMsg);
		});
	}

}
