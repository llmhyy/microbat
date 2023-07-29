package microbat.handler;

import java.util.HashSet;
import java.util.List;
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
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.views.MicroBatViews;
import microbat.views.PathView;
import microbat.views.TraceView;

public class DebugPilotHandler extends AbstractHandler {
	
	protected TraceView buggyView;
	protected PathView pathView;
	
	protected Stack<NodeFeedbacksPair> userFeedbackRecords = new Stack<>();
	
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
		settings.setTrace(buggyTrace);
		
		final TraceNode outputNode = outputs.get(0).isConditionResult() ? DebugInfo.getNodeFeedbackPair().getNode() : this.getOutputNode(outputs.get(0));
		settings.setOutputNode(outputNode);
		
//		final String propagatorName = Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.PROPAGATOR_TYPE_KEY);
//		if (propagatorName.equals("")) {
//			Log.printMsg(getClass(), "Please setup the propagator type in Preference -> Microbat Debugging -> Debug Pilot Settings");
//			return;
//		}
//		final PropagatorType propagatorType = PropagatorType.valueOf(propagatorName);
//		
//		final String pathFinderName = Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.PATHFINDER_TYPE_KEY);
//		if (pathFinderName.equals("")) {
//			Log.printMsg(getClass(), "Please setup the path finder type in Preference -> Microbat Debugging -> Debug Pilot Settings");
//			return;
//		}
//		final PathFinderType pathFinderType = PathFinderType.valueOf(pathFinderName);
//		
//		// Locate outputs
//		final List<VarValue> outputs = DebugInfo.getOutputs();
//		
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
					 * 		Double check the previous node.
					 * 		If previous feedback is inaccurate, then adjust it
					 * 		If user insist the previous feedback is accurate, then omission bug occur
					 * 2. Invalid feedback (cannot find next node in trace based on give feedback)
					 * 		Double check with user, if user insist, then omission bug occur
					 * 3. Valid feedback
					 * 		Record it in the stack
					 */
					if (correctingFeedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
						Log.printMsg(this.getClass(), "You give CORRECT feedback at path: " + pathIdx + " node: " + userNode.getOrder());
						Log.printMsg(getClass(), "Please confirm again you choice");
						NodeFeedbacksPair newFeedback = this.waitForFeedback();
						if (newFeedback.equals(correctingFeedback)) {
							// Omission bug detected
							NodeFeedbacksPair previousFeedback = this.userFeedbackRecords.peek();
							this.reportOmissionBug(userNode, previousFeedback, proposedPath);
							isEnd = true;
							break;
						} else {
							// Inaccurate feedback given, restart the process again
							this.userFeedbackRecords.clear();
							pathIdx = 0;
							correctingFeedback = newFeedback;
						}
					} else if (!this.isValidFeedback(correctingFeedback)) {
						Log.printMsg(this.getClass(), "Cannot find next node. Please double check you feedback at path: " + pathIdx + " node: " + userNode.getOrder());
						NodeFeedbacksPair newFeedback = this.waitForFeedback();
						if (newFeedback.equals(correctingFeedback)) {
							// Omission bug detected
							NodeFeedbacksPair previousFeedback = this.userFeedbackRecords.peek();
							this.reportOmissionBug(userNode, previousFeedback, proposedPath);
							isEnd = true;
							break;
						} else {
							// Inaccurate feedback given, restart the process again
							this.userFeedbackRecords.clear();
							pathIdx = 0;
							correctingFeedback = newFeedback;
						}
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
	
	protected void reportOmissionBug(final TraceNode startNode, final NodeFeedbacksPair feedback, final FeedbackPath path) {
		final FeedbackPath newPath = new FeedbackPath();
		for (NodeFeedbacksPair pair : path) {
			if (pair.getNode().getOrder() >= startNode.getOrder()) {
				newPath.addPair(pair);
			}
		}
		if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			this.reportMissingBranchOmissionBug(startNode, feedback.getNode());
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			VarValue varValue = feedback.getFeedbacks().get(0).getOption().getReadVar();
			this.reportMissingAssignmentOmissionBug(startNode, feedback.getNode(), varValue);
		}
		this.pathView.setActionPath(newPath);
		this.updateView();
	}
	protected void reportMissingBranchOmissionBug(final TraceNode startNode, final TraceNode endNode) {
		Log.printMsg(this.getClass(), "-------------------------------------------");
		Log.printMsg(this.getClass(), "Omission bug detected");
		Log.printMsg(this.getClass(), "Scope begin: " + startNode.getOrder());
		Log.printMsg(this.getClass(), "Scope end: " + endNode.getOrder());
		Log.printMsg(this.getClass(), "Omission Type: Missing Branch");
		Log.printMsg(this.getClass(), "-------------------------------------------");
		startNode.reason = StepExplaination.MISS_BRANCH;
		endNode.reason = StepExplaination.MISS_BRANCH;
	}
	
	protected void reportMissingAssignmentOmissionBug(final TraceNode startNode, final TraceNode endNode, final VarValue var) {
		Log.printMsg(this.getClass(), "-------------------------------------------");
		Log.printMsg(this.getClass(), "Omission bug detected");
		Log.printMsg(this.getClass(), "Scope begin: " + startNode.getOrder());
		Log.printMsg(this.getClass(), "Scope end: " + endNode.getOrder());
		Log.printMsg(this.getClass(), "Omission Type: Missing Assignment of " + var.getVarName());
		Log.printMsg(this.getClass(), "-------------------------------------------");
		startNode.reason = StepExplaination.MISS_DEF(var.getVarName());
		endNode.reason = StepExplaination.MISS_DEF(var.getVarName());
	}

}
