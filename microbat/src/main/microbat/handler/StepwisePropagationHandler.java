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
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.SPP.SPP;
import microbat.probability.SPP.pathfinding.ActionPath;
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
		List<VarValue> inputs = DebugInfo.getInputs();
		List<VarValue> outputs = DebugInfo.getOutputs();
		VarValue output = outputs.get(0);
		
		final TraceNode outputNode = this.getStartingNode(buggyView.getTrace(), outputs.get(0));
		UserFeedback initFeedback = new UserFeedback(new ChosenVariableOption(output, null), UserFeedback.WRONG_VARIABLE_VALUE);
		NodeFeedbacksPair intiPair = new NodeFeedbacksPair(outputNode, initFeedback);
//		this.userFeedbackRecords.add(intiPair);
		
		// Set up the propagator that perform propagation,
		// with initial feedback indicating the output variable  is wrong
		SPP spp = new SPP(buggyView.getTrace(), inputs, outputs);
		
		int feedbackCounts = 0;
		TraceNode currentNode = outputNode;
		
		boolean isEnd = false;
		// Keep doing propagation until the root cause is found
		while(!DebugInfo.isRootCauseFound() && !DebugInfo.isStop() && !isEnd) {
			System.out.println("---------------------------------- " + feedbackCounts + " iteration");
			System.out.println("Propagating ...");
			
			// Perform propagation
			spp.updateFeedbacks(userFeedbackRecords);
			spp.propagate();
			
			// Root cause prediction
			TraceNode rootCause = spp.proposeRootCause();
			System.out.println("Proposed Root Cause: " + rootCause.getOrder());
			
			// Handle the case that root cause is at the downstream of current node
//			if (rootCause.getOrder() > currentNode.getOrder()) {
//				System.out.println();
//				System.out.println("Proposed a wrong root cause becuase it is the downstream of current node: " + currentNode.getOrder());
//				System.out.println("Give feedback based on probability:");
//				UserFeedback predictedFeedback = spp.giveFeedback(currentNode);
//				System.out.println(predictedFeedback);
//				NodeFeedbacksPair userPair = askForFeedback(currentNode);
//				UserFeedback userFeedback = userPair.getFeedbacks().get(0);
//				currentNode = TraceUtil.findNextNode(currentNode, userFeedback, buggyView.getTrace());
//				continue;
//			}
			
			System.out.println("Path finding ...");
			ActionPath userPath = new ActionPath(userFeedbackRecords);
			final ActionPath path = spp.suggestPath(currentNode, rootCause, userPath);
			
			System.out.println();
			for (NodeFeedbacksPair section : path) {
				System.out.println("Debug: " + section);
			}
			System.out.println();
			
			// Ensure that user current location is on the path
			if (!path.contains(currentNode)) {
				throw new RuntimeException("Suggested path does not contain current node");
			}
			
			for (int idx=0; idx<path.getLength(); idx++) {
				final NodeFeedbacksPair action = path.get(idx);
				
				// Go to the current location
				final TraceNode node = action.getNode();
				if (!node.equals(currentNode)) {
					continue;
				}
				
				this.jumpToNode(currentNode);
				System.out.println("Predicted feedback: ");
				System.out.println(action);
				
				// Obtain feedback from user
				NodeFeedbacksPair userFeedbackPair = askForFeedback(currentNode);
				final UserFeedback predictedFeedback = action.getFeedbacks().get(0);
				
				// Feedback predicted correctly
				if (userFeedbackPair.containsFeedback(predictedFeedback)) {
					currentNode = TraceUtil.findNextNode(currentNode, predictedFeedback, buggyView.getTrace());
					this.userFeedbackRecords.add(userFeedbackPair);
					continue;
				}
				
				// Feedback is predicted wrongly
						
				/*
				 *  If the feedback is CORRECT, there are two reasons:
				 *  1. User give wrong feedback
				 *  2. Omission bug occur
				 */
				if (userFeedbackPair.getFeedbackType().equals(UserFeedback.CORRECT)) {
					// We first assume user give a wrong feedback
					NodeFeedbacksPair prevPair = userFeedbackRecords.peek();
					UserFeedback prevFeedback = prevPair.getFeedbacks().get(0);
					TraceNode prevNode = prevPair.getNode();
					jumpToNode(prevNode);
					System.out.println("[SPP] Please confirm again the feedback of this node: " + node.getOrder());
					NodeFeedbacksPair correctingFeedbackPair = askForFeedback(node);
					if (prevPair.equals(correctingFeedbackPair)) {
						// User insist feedback is correct, omission bug confirmed
						if (prevFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
							final VarValue var = prevFeedback.getOption().getReadVar();
							reportMissingAssignmentOmissionBug(node, prevNode, var);
						} else {
							reportMissingBranchOmissionBug(node, prevNode);
						}
						isEnd = true;
					} else {
						// User confirm that previous feedback is inaccurate, it is possible that they
						// give more than one inaccurate feedback, so that we loop to find out the last accurate feedback
						userFeedbackRecords.pop();
						while (!userFeedbackRecords.isEmpty()) {
							prevPair = userFeedbackRecords.peek();
							prevNode = prevPair.getNode();
							prevFeedback = prevPair.getFeedbacks().get(0);
							jumpToNode(prevNode);
							System.out.println("[SPP] Please confirm again the feedback of this node: " + node.getOrder());
							correctingFeedbackPair = askForFeedback(node);
							if (correctingFeedbackPair.equals(prevPair)) {
								// Last accurate feedback located
								break;
							}
							userFeedbackRecords.pop();
						}
						currentNode = TraceUtil.findNextNode(prevNode, prevFeedback, this.buggyView.getTrace());
					}
					break;
				}
				
				UserFeedback userFeedback = userFeedbackPair.getFeedbacks().get(0);
				TraceNode nextNode = TraceUtil.findNextNode(currentNode, userFeedback, buggyView.getTrace());
				
				/*
				 * If the feedback is wrong path and there are no control dominator, 
				 * there are several reasons:
				 * 1. User give a wrong feedback
				 * 2. Omission bug msing branch
				 */
				if (nextNode == null && userFeedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
					// First assume user give a wrong feedback
					System.out.println("[SPP] There are no control dominator of this step. Can you confirm again the feedback of node: " + node.getOrder());
					final NodeFeedbacksPair correctingFeedbackPair = askForFeedback(node);
					if (correctingFeedbackPair.equals(userFeedbackPair)) {
						// User insist feedback is correct, omission bug confirmed
						TraceNode beginNode = node.getInvocationParent();
						if (beginNode == null) {
							beginNode = buggyView.getTrace().getTraceNode(1);
						}
						reportMissingBranchOmissionBug(beginNode, node);
						isEnd = true;
						break;
					} else {
						// Check is the feedback match with the predicted feedback
						if (correctingFeedbackPair.containsFeedback(predictedFeedback)) {
							currentNode = TraceUtil.findNextNode(currentNode, predictedFeedback, buggyView.getTrace());
							continue;
						} else {
							// if not, then process it the same way as wrong prediction
							userFeedbackPair = correctingFeedbackPair;
							userFeedback = userFeedbackPair.getFeedbacks().get(0);
						}
					}
				}
				
				// Handle wrong prediction
				this.userFeedbackRecords.add(userFeedbackPair);
				currentNode = TraceUtil.findNextNode(currentNode, userFeedback, this.buggyView.getTrace());
				break;
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
		return !DebugInfo.getInputs().isEmpty() && !DebugInfo.getOutputs().isEmpty();
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
		System.out.println("Please give an feedback for node: " + node.getOrder());
		DebugInfo.waitForFeedbackOrRootCauseOrStop();
		NodeFeedbacksPair userPairs = DebugInfo.getNodeFeedbackPair();
		DebugInfo.clearNodeFeedbackPairs();
		System.out.println();
		System.out.println("UserFeedback:");
		System.out.println(userPairs);
		return userPairs;
	}
	
	protected void reportMissingBranchOmissionBug(final TraceNode startNode, final TraceNode endNode) {
		System.out.println("-------------------------------------------");
		System.out.println("Omission bug detected");
		System.out.println("Scope begin: " + startNode.getOrder());
		System.out.println("Scope end: " + endNode.getOrder());
		System.out.println("Omission Type: Missing Branch");
		System.out.println("-------------------------------------------");
	}
	
	protected void reportMissingAssignmentOmissionBug(final TraceNode startNode, final TraceNode endNode, final VarValue var) {
		System.out.println("-------------------------------------------");
		System.out.println("Omission bug detected");
		System.out.println("Scope begin: " + startNode.getOrder());
		System.out.println("Scope end: " + endNode.getOrder());
		System.out.println("Omission Type: Missing Assignment of " + var.getVarName());
		System.out.println("-------------------------------------------");
	}
}
