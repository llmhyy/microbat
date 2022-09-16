package microbat.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.baseline.encoders.NodeFeedbackPair;
import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class BaselineHandler extends AbstractHandler {

	TraceView traceView = null;
//	UserFeedback userFeedback = null;
	
	public static List<VarValue> inputs = null;
	public static List<VarValue> outputs = null;
	
	private static UserFeedback manualFeedback = null;
	private static TraceNode feedbackNode = null;
	
	private static boolean rootCauseFound = false;
	private static String testCaseMethod = "";
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setup();
				if (traceView == null) {
					System.out.println("traceView is null");
					return null;
				}
				
				int noOfFeedbacks = 0;
				
				Trace trace = traceView.getTrace();
				
				// Setup the probability encoder
				ProbabilityEncoder encoder = new ProbabilityEncoder(trace);
				encoder.setInputVars(BaselineHandler.inputs);
				encoder.setOutputVars(BaselineHandler.outputs);
				encoder.setup();
				
				while (!BaselineHandler.rootCauseFound) {
					System.out.println("---------------------------------- " + noOfFeedbacks + " iteration");
					
					// Calculate probability
					encoder.encode();
					
					// Predict root cause
					TraceNode prediction = encoder.getMostErroneousNode();
					
					// Visualize
					jumpToNode(prediction);
					
					System.out.println("Prediction: " + prediction.getOrder());
					
					// Wait for the feedback
					System.out.println("Please give a feedback");
					while (!BaselineHandler.isManualFeedbackReady() && !BaselineHandler.rootCauseFound) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							
						}
					}
				
					if (BaselineHandler.rootCauseFound) {
						printReport(BaselineHandler.testCaseMethod, noOfFeedbacks);
						break;
					}
					
					// Send feedback to Probability Encoder
					UserFeedback feedback = BaselineHandler.manualFeedback;
					TraceNode feedbackNode = BaselineHandler.feedbackNode;
					NodeFeedbackPair nodeFeedbackPair = new NodeFeedbackPair(feedbackNode, feedback);
					BaselineHandler.resetManualFeedback();
					
					ProbabilityEncoder.addFeedback(nodeFeedbackPair);;
					noOfFeedbacks += 1;
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
		
		BaselineHandler.rootCauseFound = false;
	}
	
	private void printReport(final String testCaseMethod, final int noOfFeedbacks) {
		System.out.println("---------------------------------");
		System.out.println("Debug Report: Test Case Method Name: " + testCaseMethod);
		System.out.println("Number of feedbacks: " + noOfFeedbacks);
		System.out.println("---------------------------------");
	}
	
	public static void setManualFeedback(UserFeedback manualFeedback, TraceNode node) {
		BaselineHandler.manualFeedback = manualFeedback;
		BaselineHandler.feedbackNode = node;
	}
	
	public static void resetManualFeedback() {
		BaselineHandler.manualFeedback = null;
		BaselineHandler.feedbackNode = null;
	}
	
	public static void clearData() {
		BaselineHandler.inputs = null;
		BaselineHandler.outputs = null;
	}
	
	public static void addInputs(List<VarValue> inputs) {
		if (BaselineHandler.inputs == null) {
			BaselineHandler.inputs = new ArrayList<>();
		}
		BaselineHandler.inputs.addAll(inputs);
		
		for (VarValue input : BaselineHandler.inputs) {
			System.out.println("BaselineHandler: Selected Inputs: " + input.getVarID());
		}
	}
	
	public static void printIO() {
		for (VarValue input : BaselineHandler.inputs) {
			System.out.println("BaselineHandler: Selected Inputs: " + input.getVarID());
		}
		for (VarValue output : BaselineHandler.outputs) {
			System.out.println("BaselineHandler: Selected Outputs: " + output.getVarID());
		}
	}
	
	public static void addOutpus(List<VarValue> outputs) {
		if (BaselineHandler.outputs == null) {
			BaselineHandler.outputs = new ArrayList<>();
		}
		BaselineHandler.outputs.addAll(outputs);
		
		for (VarValue output : BaselineHandler.outputs) {
			System.out.println("BaselineHandler: Selected Outputs: " + output.getVarID());
		}
	}
	
	public boolean isReady() {
		if (BaselineHandler.inputs == null || BaselineHandler.outputs == null) {
			return false;
		}
		
		return !(BaselineHandler.inputs.isEmpty() || BaselineHandler.outputs.isEmpty());
	}
	
	public static void clearIO() {
		if (BaselineHandler.inputs != null) {
			BaselineHandler.inputs.clear();
		}
		
		if (BaselineHandler.outputs != null) {
			BaselineHandler.outputs.clear();
		}
		
		System.out.println("BaselineHandler: Clear IO");
	}
	
	public static boolean isManualFeedbackReady() {
		return BaselineHandler.manualFeedback != null && BaselineHandler.feedbackNode != null;
	}
	
	public static void setRootCauseFound(boolean found) {
		BaselineHandler.rootCauseFound = found;
	}
	
	public static void setTestCaseMethod(final String testCaseMethod) {
		BaselineHandler.testCaseMethod = testCaseMethod;
	}
}
