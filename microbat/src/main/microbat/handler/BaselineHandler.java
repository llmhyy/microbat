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

import microbat.baseline.probpropagation.NodeFeedbackPair;
import microbat.baseline.probpropagation.BeliefPropagation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class BaselineHandler extends AbstractHandler implements RequireIO {

	TraceView traceView = null;
//	UserFeedback userFeedback = null;
	
	private List<VarValue> inputs = new ArrayList<>();
	private List<VarValue> outputs = new ArrayList<>();
	
	private static UserFeedback manualFeedback = null;
	private static TraceNode feedbackNode = null;
	
	public static boolean rootCauseFound = false;
	private static String testCaseMethod = "";

	private static boolean registeredFlag = false;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				if (!registeredFlag) {
					registerHandler();
					return Status.OK_STATUS;
				}
				
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
				encoder.setInputVars(inputs);
				encoder.setOutputVars(outputs);
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
						printReport(noOfFeedbacks);
						break;
					}
					
					// Send feedback to Probability Encoder
					UserFeedback feedback = BaselineHandler.manualFeedback;
					TraceNode feedbackNode = BaselineHandler.feedbackNode;
					NodeFeedbackPair nodeFeedbackPair = new NodeFeedbackPair(feedbackNode, feedback);
					BaselineHandler.resetManualFeedback();
					
					BeliefPropagation.addFeedback(nodeFeedbackPair);;
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
		DebugFeedbackView.registerHandler(this);
	}
	
	public void registerHandler() {
		DebugFeedbackView.registerHandler(this);
		BaselineHandler.registeredFlag = true;
		
		System.out.println();
		System.out.println("BaselineHandler is registered to buttons");
		System.out.println("Now please select the inputs and outputs");
	}
	
	private void printReport(final int noOfFeedbacks) {
		System.out.println("---------------------------------");
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
	
	public boolean isReady() {
		if (this.inputs.isEmpty()) {
			throw new RuntimeException("No inputs provided");
		}
		
		if (this.outputs.isEmpty()) {
			throw new RuntimeException("No outputs provided");
		}
		
		return true;
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

	@Override
	public void addInputs(Collection<VarValue> inputs) {
		this.inputs.addAll(inputs);
		
		for (VarValue input : this.inputs) {
			System.out.println("BaselineHandler: Selected Inputs: " + input.getVarID());
		}
	}

	@Override
	public void addOutputs(Collection<VarValue> outputs) {
		this.outputs.addAll(outputs);
		
		for (VarValue output : this.outputs) {
			System.out.println("BaselineHandler: Selected outputs: " + output.getVarID());
		}
		
	}

	@Override
	public void printIO() {
		for (VarValue input : this.inputs) {
			System.out.println("BaselineHandler: Selected Inputs: " + input.getVarID());
		}
		for (VarValue output : this.outputs) {
			System.out.println("BaselineHandler: Selected Outputs: " + output.getVarID());
		}
	}

	@Override
	public void clearData() {
		this.inputs.clear();
		this.outputs.clear();
		System.out.println("BaselineHandler: Clear IO");
	}
}
