package microbat.handler;

import java.util.ArrayList;
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
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.SPP.DebugPilot;
import microbat.probability.SPP.NodeNotInPathException;
import microbat.probability.SPP.pathfinding.ActionPath;
import microbat.probability.SPP.pathfinding.PathFinderType;
import microbat.probability.SPP.propagation.PropagatorType;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class StepwisePropagationHandler extends AbstractHandler {

	protected TraceView buggyView = null;
	private Stack<NodeFeedbacksPair> userFeedbackRecords = new Stack<>();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return stepwisePropagation();
			}
			
		};
		job.schedule();
		return null;
	}
	
	protected IStatus stepwisePropagation() {
		// Get the trace view
		this.setup();
		
		System.out.println();
		System.out.println("---------------------------------------------");
		System.out.println("\t Stepwise Probability Propagation");
		System.out.println();
		
		// Check is the trace ready
		if (this.buggyView.getTrace() == null) {
			System.out.println("Please setup the trace before propagation");
			return Status.OK_STATUS;
		}
		
		// Check is the IO ready
		if (!this.isIOReady()) {
			System.out.println("Please provide the inputs and the outputs");
			return Status.OK_STATUS;
		}
		
		// Obtain the inputs and outputs from users
		// We only consider the first output
		final List<VarValue> inputs = DebugInfo.getInputs();
		final List<VarValue> outputs = DebugInfo.getOutputs();

		VarValue output = outputs.get(0);		
		TraceNode outputNode = null;
		if (output.getVarID().startsWith("CR_")) {
			// Initial feedback is wrong path
			NodeFeedbacksPair initPair = DebugInfo.getNodeFeedbackPair();
			outputNode = initPair.getNode();
		} else {
			outputNode = this.getStartingNode(buggyView.getTrace(), outputs.get(0));
		}
		
		// Set up the propagator that perform propagation,
		// with initial feedback indicating the output variable  is wrong
		DebugPilot debugPilot = new DebugPilot(buggyView.getTrace(), inputs, outputs, outputNode, PropagatorType.None, PathFinderType.Random);
		
		TraceNode currentNode = outputNode;
		List<TraceNode> candidatesCurrentNodes = new ArrayList<>();
		boolean isEnd = false;
		// Keep doing propagation until the root cause is found
		while(!DebugInfo.isRootCauseFound() && !DebugInfo.isStop() && !isEnd) {
			// Perform propagation
			debugPilot.updateFeedbacks(userFeedbackRecords);
			debugPilot.multiSlicing();
			Log.printMsg(this.getClass(), "Propagating probability ...");
			long startTime = System.currentTimeMillis();
			debugPilot.propagate();
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime) / 1000;
			Log.printMsg(this.getClass(), "Propagation Duration: " + duration + " s");
			Log.printMsg(this.getClass(), "Locating root cause ...");
			debugPilot.locateRootCause(currentNode);
			Log.printMsg(this.getClass(), "Constructing path to root cause ...");
			debugPilot.constructPath();
			
			boolean needPropagateAgain = false;
			while (!needPropagateAgain && !isEnd) {
				UserFeedback predictedFeedback = null;
				if (candidatesCurrentNodes.size() > 1) {
					for (TraceNode candidateNode : candidatesCurrentNodes) {
						try {
							predictedFeedback = debugPilot.giveFeedback(candidateNode);
						} catch (NodeNotInPathException e ) {
							continue;
						}
						currentNode = candidateNode;
						break;
					}
					if (predictedFeedback == null) {
						throw new NodeNotInPathException(Log.genMsg(getClass(), "Give nodes " + candidatesCurrentNodes + " does not in path"));
					}
				} else {
					predictedFeedback = debugPilot.giveFeedback(currentNode);
				}
				Log.printMsg(this.getClass(), "--------------------------------------");
				Log.printMsg(this.getClass(), "Predicted feedback of node: " + currentNode.getOrder() + ": " + predictedFeedback.toString());
				NodeFeedbacksPair userFeedbacks = this.askForFeedback(currentNode);
				if (userFeedbacks.containsFeedback(predictedFeedback)) {
					// Feedback predicted correctly, save the feedback into record and move to next node
					this.userFeedbackRecords.add(userFeedbacks);
					currentNode = TraceUtil.findNextNode(currentNode, predictedFeedback, this.buggyView.getTrace());
				} else if (userFeedbacks.getFeedbackType().equals(UserFeedback.CORRECT)) {
					/*	If the feedback is CORRECT, there are two reasons:
					 *  1. User give wrong feedback
					 *  2. Omission bug occur
					 *  
					 *  We first assume that user give a inaccurate feedback last iteration
					 *  and ask user to correct it. Since user may give multiple inaccurate
					 *  feedbacks, so that we will keep asking until the last accurate feedback
					 *  is located or we end up at the initial step.
					 *  
					 *  If user insist the previous feedback is accurate, then we say there is 
					 *  omission bug
					 */
					Log.printMsg(this.getClass(), "You give CORRECT feedback at node: " + currentNode.getOrder());
					NodeFeedbacksPair prevRecord = this.userFeedbackRecords.peek();
					TraceNode prevNode = prevRecord.getNode();
					Log.printMsg(this.getClass(), "Please confirm the feedback at previous node.");
					NodeFeedbacksPair correctingFeedbacks = this.askForFeedback(prevNode);
					if (correctingFeedbacks.equals(prevRecord)) {
						// Omission bug confirmed
						this.reportOmissionBug(currentNode, correctingFeedbacks);
						isEnd = true;
					}  else {
						boolean lastAccurateFeedbackLocated = false;
						this.userFeedbackRecords.pop();
						while (!lastAccurateFeedbackLocated && !isEnd) {
							prevRecord = this.userFeedbackRecords.peek();
							prevNode = prevRecord.getNode();
							Log.printMsg(this.getClass(), "Please confirm the feedback at previous node.");
							correctingFeedbacks = this.askForFeedback(prevNode);
							if (correctingFeedbacks.equals(prevRecord)) {
								lastAccurateFeedbackLocated = true;
								currentNode = TraceUtil.findNextNode(prevNode, correctingFeedbacks.getFeedbacks().get(0), this.buggyView.getTrace());
								Log.printMsg(this.getClass(), "Last accurate feedback located. Please start giveing feedback from node: " + currentNode.getOrder());
								continue;
							}
							this.userFeedbackRecords.pop();
							if (this.userFeedbackRecords.isEmpty()) {
								// Reach initial feedback
								Log.printMsg(this.getClass(), "You are going to reach the initialize feedback which assumed to be accurate");
								Log.printMsg(this.getClass(), "Pleas start giving from node: "+prevNode.getOrder());
								Log.printMsg(this.getClass(), "If the initial feedback is inaccurate, please start the whole process again");
								currentNode = prevNode;
								lastAccurateFeedbackLocated = true;
							}
						}
					}
				} else if (TraceUtil.findNextNode(currentNode, userFeedbacks.getFirstFeedback(), this.buggyView.getTrace()) == null) {
					/* Next node is null. Possible reasons:
					 * 1. Wrong feedback is given
					 * 2. Omission bug occur
					 * 
					 * First assume a wrong feedback is given and ask user
					 * to correct it. After correction, if the feedback
					 * match with predicted feedback, then continue the process
					 * as if nothing happen. If the feedback mismatch, then
					 * handle it the same as wrong prediction.
					 * 
					 * If the user insist the feedback is accurate, then
					 * omission bug confirm
					 */
					Log.printMsg(this.getClass(), "Cannot find next node. Please double check you feedback at node: " + currentNode.getOrder());
					NodeFeedbacksPair correctingFeedbacks = this.askForFeedback(currentNode);
					if (correctingFeedbacks.equals(userFeedbacks)) {
						// Omission bug confirmed
						final TraceNode startNode = currentNode.getInvocationParent() == null ? buggyView.getTrace().getTraceNode(1) : currentNode.getInvocationParent();
						this.reportOmissionBug(startNode, correctingFeedbacks);
						isEnd = true;
					} else {
						Log.printMsg(this.getClass(), "Wong prediction on feedback, start propagation again");
						needPropagateAgain = true;
						this.userFeedbackRecords.add(correctingFeedbacks);
						currentNode = TraceUtil.findNextNode(currentNode, correctingFeedbacks.getFirstFeedback(), this.buggyView.getTrace());
					}
				} else {
					/*	Wrong prediction on feedback
					 *  We need to record it and start the propagation again
					 */
					Log.printMsg(this.getClass(), "Wong prediction on feedback, start propagation again");
					needPropagateAgain = true;
					this.userFeedbackRecords.add(userFeedbacks);
					candidatesCurrentNodes.clear();
					if (userFeedbacks.getFeedbacks().size() > 1) {
						for (UserFeedback feedback : userFeedbacks.getFeedbacks()) {
							final TraceNode nextNode = TraceUtil.findNextNode(currentNode, feedback, this.buggyView.getTrace());
							candidatesCurrentNodes.add(nextNode);
						}
					} else {
						currentNode = TraceUtil.findNextNode(currentNode, userFeedbacks.getFirstFeedback(), this.buggyView.getTrace());
					}
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	
	protected void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				buggyView = MicroBatViews.getTraceView();
			}
		});
	}
	
	protected boolean isIOReady() {
		return !DebugInfo.getInputs().isEmpty() && !(DebugInfo.getOutputs().isEmpty());
	}
	
	protected void jumpToNode(final TraceNode targetNode) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				Trace buggyTrace = buggyView.getTrace();
				buggyView.jumpToNode(buggyTrace, targetNode.getOrder(), true);
		    }
		});
	}

	protected void printReport(final int noOfFeedbacks) {
		System.out.println("---------------------------------");
		System.out.println("Number of feedbacks: " + noOfFeedbacks);
		System.out.println("---------------------------------");
	}
	
	protected TraceNode getStartingNode(final Trace trace, final VarValue output) {
		for (int order = trace.size(); order>=0; order--) {
			TraceNode node = trace.getTraceNode(order);
			final String varID = output.getVarID();
			if (node.isReadVariablesContains(varID)) {
				return node;
			} else if (node.isWrittenVariablesContains(varID)) {
				return node;
			}
		}
		return null;
	}
	
	protected NodeFeedbacksPair askForFeedback(final TraceNode node) {
		this.jumpToNode(node);
		Log.printMsg(this.getClass(), "Please give an feedback for node: " + node.getOrder());
		DebugInfo.waitForFeedbackOrRootCauseOrStop();
		NodeFeedbacksPair userPairs = DebugInfo.getNodeFeedbackPair();
		DebugInfo.clearNodeFeedbackPairs();
		System.out.println();
		Log.printMsg(this.getClass(), "UserFeedback: " + userPairs);
		return userPairs;
	}
	
	protected void reportOmissionBug(final TraceNode startNode, final NodeFeedbacksPair feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			this.reportMissingBranchOmissionBug(startNode, feedback.getNode());
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			VarValue varValue = feedback.getFeedbacks().get(0).getOption().getReadVar();
			this.reportMissingAssignmentOmissionBug(startNode, feedback.getNode(), varValue);
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
