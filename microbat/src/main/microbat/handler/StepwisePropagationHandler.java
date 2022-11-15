package microbat.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import debuginfo.DebugInfo;
import debuginfo.NodeFeedbackPair;
import microbat.baseline.probpropagation.StepwisePropagator;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class StepwisePropagationHandler extends AbstractHandler {

	TraceView traceView = null;
	
//	private List<VarValue> inputs = new ArrayList<>();
//	private List<VarValue> outputs = new ArrayList<>();
//	
	private static boolean registerFlag = false;
//	
//	private static UserFeedback manualFeedback = null;
//	private static TraceNode feedbackNode = null;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
//				if (!registerFlag) {
//					registerHandler();
//					return Status.OK_STATUS;
//				}
				
				setup();
				
				System.out.println();
				System.out.println("---------------------------------------------");
				System.out.println("\t Stepwise Probability Propagation");
				System.out.println();
				
				// Check is the trace ready
				if (traceView.getTrace() == null) {
					System.out.println("Please setup the trace before propagation");
					return Status.OK_STATUS;
				}
				
				// Check is the IO ready
				if (!isIOReady()) {
					System.out.println("Please provide the inputs and the outputs");
					return Status.OK_STATUS;
				}
				
				List<VarValue> inputs = DebugInfo.getInputs();
				List<VarValue> outputs = DebugInfo.getOutputs();
				StepwisePropagator propagator = new StepwisePropagator(traceView.getTrace(), inputs, outputs);

				int feedbackCounts = 0;
				while(!DebugInfo.isRootCauseFound()) {
					System.out.println("---------------------------------- " + feedbackCounts + " iteration");
					System.out.println("Propagation Start");
					propagator.propagate();
					
					System.out.println("Propagation End");
					TraceNode rootCause = propagator.proposeRootCause();
					jumpToNode(rootCause);
					System.out.println("Proposed Root Cause: " + rootCause.getOrder());
					
					System.out.println("Please give a feedback");
					DebugInfo.waitForFeedbackOrRootCause();
					
					if (DebugInfo.isRootCauseFound()) {
						printReport(feedbackCounts);
						break;
					}
//					while(!StepwisePropagationHandler.isManualFeedbackReady() && !BaselineHandler.rootCauseFound) {
//						try {
//							Thread.sleep(200);
//						} catch (InterruptedException e) {
//							
//						}
//					}
//					
//					if (BaselineHandler.rootCauseFound) {
//						printReport(feedbackCounts);
//						break;
//					}
//					
//					// Send feedback to stepwise propagator
//					UserFeedback feedback = StepwisePropagationHandler.manualFeedback;
//					TraceNode feedbackNode = StepwisePropagationHandler.feedbackNode;
//					NodeFeedbackPair nodeFeedbackPair = new NodeFeedbackPair(feedbackNode, feedback);
//					StepwisePropagationHandler.resetManualFeedback();
					
					NodeFeedbackPair nodeFeedbackPair = DebugInfo.getNodeFeedbackPair();
					propagator.responseToFeedback(nodeFeedbackPair);
					feedbackCounts +=1;
				}
				
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		return null;
	}
	
	private void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				traceView = MicroBatViews.getTraceView();
			}
		});
	}
	
	public boolean isIOReady() {
		return !DebugInfo.getInputs().isEmpty() && !DebugInfo.getOutputs().isEmpty();
	}
	
	private void jumpToNode(final TraceNode targetNode) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				Trace buggyTrace = traceView.getTrace();
				traceView.jumpToNode(buggyTrace, targetNode.getOrder(), true);
		    }
		});
	}

//	@Override
//	public void registerHandler() {
//		DebugFeedbackView.registerHandler(this);
//		StepwisePropagationHandler.registerFlag = true;
//		
//		System.out.println();
//		System.out.println("StepwisePropagationHandler is now registered to buttons");
//		System.out.println("Please select inputs and outputs");
//	}

//	@Override
//	public void addInputs(Collection<VarValue> inputs) {
//		this.inputs.addAll(inputs);
//		for (VarValue input : this.inputs) {
//			System.out.println("StepwisePropagationHandler: Selected Inputs: " + input.getVarID());
//		}
//	}
//
//	@Override
//	public void addOutputs(Collection<VarValue> outputs) {
//		this.outputs.addAll(outputs);
//		for (VarValue output : this.outputs) {
//			System.out.println("StepwisePropagationHandler: Selected Outputs: " + output.getVarID());
//		}
//	}
//
//	@Override
//	public void printIO() {
//		for (VarValue input : this.inputs) {
//			System.out.println("StepwisePropagationHandler: Selected Inputs: " + input.getVarID());
//		}
//		for (VarValue output : this.outputs) {
//			System.out.println("StepwisePropagationHandler: Selected Outputs: " + output.getVarID());
//		}
//	}
//
//	@Override
//	public void clearData() {
//		this.inputs = null;
//		this.outputs = null;
//	}
	
//	public static boolean isManualFeedbackReady() {
//		return StepwisePropagationHandler.manualFeedback != null && StepwisePropagationHandler.feedbackNode != null;
//	}
//	
//	public static void setManualFeedback(UserFeedback manualFeedback, TraceNode node) {
//		StepwisePropagationHandler.manualFeedback = manualFeedback;
//		StepwisePropagationHandler.feedbackNode = node;
//	}
//	
//	public static void resetManualFeedback() {
//		StepwisePropagationHandler.manualFeedback = null;
//		StepwisePropagationHandler.feedbackNode = null;
//	}

	private void printReport(final int noOfFeedbacks) {
		System.out.println("---------------------------------");
		System.out.println("Number of feedbacks: " + noOfFeedbacks);
		System.out.println("---------------------------------");
	}
}
