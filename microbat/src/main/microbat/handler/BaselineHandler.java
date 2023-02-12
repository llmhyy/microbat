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
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.BP.BeliefPropagation;
import microbat.recommendation.UserFeedback;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class BaselineHandler extends AbstractHandler {

	TraceView traceView = null;
//	UserFeedback userFeedback = null;
	

//	private List<VarValue> inputs = new ArrayList<>();
//	private List<VarValue> outputs = new ArrayList<>();
//	
//	private static UserFeedback manualFeedback = null;
//	private static TraceNode feedbackNode = null;
	
//	private static boolean rootCauseFound = false;
	private static String testCaseMethod = "";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				setup();
				
				if (traceView == null) {
					System.out.println("traceView is null");
					return Status.OK_STATUS;
				}
				
				if (!isReady()) {
					return Status.OK_STATUS;
				}
				
				int noOfFeedbacks = 0;
				
				Trace trace = traceView.getTrace();
				
				// Setup the probability encoder
				BeliefPropagation encoder = new BeliefPropagation(trace);
				
				// Collect IO from users
				List<VarValue> inputs = DebugInfo.getInputs();
				encoder.setInputVars(inputs);
				List<VarValue> outputs = DebugInfo.getOutputs();
				encoder.setOutputVars(outputs);
				
				encoder.setup();
				
				while (!DebugInfo.isRootCauseFound()) {
					System.out.println("---------------------------------- " + noOfFeedbacks + " iteration");
					
					// Calculate probability
					encoder.encode();
					
					// Predict root cause
					TraceNode prediction = encoder.getMostErroneousNode();
					
					// Visualize
					jumpToNode(prediction);
					
					System.out.println("Prediction: " + prediction.getOrder());
					
					// Wait for the feedback
//					System.out.println("Please give a feedback");
					DebugInfo.waitForFeedbackOrRootCause();
					
					if (DebugInfo.isRootCauseFound()) {
						printReport(noOfFeedbacks);
						break;
					}
					
//					NodeFeedbackPair pair = DebugInfo.getNodeFeedbackPairs();
//					BeliefPropagation.addFeedback(pair);
					noOfFeedbacks += 1;
//					while (!BaselineHandler.isManualFeedbackReady() && !BaselineHandler.rootCauseFound) {
//						try {
//							Thread.sleep(200);
//						} catch (InterruptedException e) {
//							
//						}
//					}
//				
//					if (BaselineHandler.rootCauseFound) {
//						printReport(noOfFeedbacks);
//						break;
//					}
//					
//					// Send feedback to Probability Encoder
//					UserFeedback feedback = BaselineHandler.manualFeedback;
//					TraceNode feedbackNode = BaselineHandler.feedbackNode;
//					NodeFeedbackPair nodeFeedbackPair = new NodeFeedbackPair(feedbackNode, feedback);
//					BaselineHandler.resetManualFeedback();
//					
//					BeliefPropagation.addFeedback(nodeFeedbackPair);;
//					noOfFeedbacks += 1;
				}

				
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		return null;
	}
	
	private void jumpToNode(final TraceNode targetNode) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				traceView.jumpToNode(traceView.getTrace(), targetNode.getOrder(), true);
		    }
		});
	}
	
	private void setup() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				traceView = MicroBatViews.getTraceView();
			}
			
		});
		
//		BaselineHandler.rootCauseFound = false;
//		DebugFeedbackView.registerHandler(this);
	}
	
//	public void registerHandler() {
//		DebugFeedbackView.registerHandler(this);
//		BaselineHandler.registeredFlag = true;
//		
//		System.out.println();
//		System.out.println("BaselineHandler is registered to buttons");
//		System.out.println("Now please select the inputs and outputs");
//	}
	
	private void printReport(final int noOfFeedbacks) {
		System.out.println("---------------------------------");
		System.out.println("Number of feedbacks: " + noOfFeedbacks);
		System.out.println("---------------------------------");
	}
	
//	public static void setManualFeedback(UserFeedback manualFeedback, TraceNode node) {
//		BaselineHandler.manualFeedback = manualFeedback;
//		BaselineHandler.feedbackNode = node;
//	}
//	
//	public static void resetManualFeedback() {
//		BaselineHandler.manualFeedback = null;
//		BaselineHandler.feedbackNode = null;
//	}
	
	public boolean isReady() {
		if (DebugInfo.getInputs().isEmpty()) {
			throw new RuntimeException("No inputs provided");
		}
		
		if (DebugInfo.getOutputs().isEmpty()) {
			throw new RuntimeException("No outputs provided");
		}
		
		return true;
	}
	
//	
//	public static void setRootCauseFound(boolean found) {
//		BaselineHandler.rootCauseFound = found;
//	}
	
	public static void setTestCaseMethod(final String testCaseMethod) {
		BaselineHandler.testCaseMethod = testCaseMethod;
	}
}
