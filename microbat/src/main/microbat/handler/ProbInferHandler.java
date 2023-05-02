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
import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.BP.BeliefPropagation;
import microbat.recommendation.UserFeedback;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class ProbInferHandler extends AbstractHandler {

	protected TraceView buggyView = null;

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				setup();
				
				if (buggyView == null) {
					System.out.println("traceView is null");
					return Status.OK_STATUS;
				}
				
				if (!isReady()) {
					return Status.OK_STATUS;
				}
				
				int noOfFeedbacks = 0;
				
				Trace trace = buggyView.getTrace();
				
				// Collect IO from users
				List<VarValue> inputs = DebugInfo.getInputs();
				List<VarValue> outputs = DebugInfo.getOutputs();
				if (inputs.isEmpty() || outputs.isEmpty()) {
					throw new RuntimeException("Please specify the inputs and outputs");
				}
				
				// Setup the probability encoder
				BeliefPropagation encoder = new BeliefPropagation(trace, inputs, outputs);
				
				
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
					NodeFeedbacksPair pair = askForFeedback(prediction);
					encoder.responseFeedbacks(pair);
					
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
				buggyView.jumpToNode(buggyView.getTrace(), targetNode.getOrder(), true);
		    }
		});
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
	
	protected void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				buggyView = MicroBatViews.getTraceView();
			}
		});
	}
	
	
	
	public boolean isReady() {
		if (DebugInfo.getInputs().isEmpty() || DebugInfo.getOutputs().isEmpty()) {
			System.out.println("Please provide the inputs and outputs");
			return false;
		}
		return true;
	}
	
}
