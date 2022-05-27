package microbat.autofeedback;

import org.eclipse.swt.widgets.Display;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.StepRecommender;
import microbat.recommendation.UserFeedback;
import microbat.util.Settings;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class AutoDebugSimulator {
	
	private Trace trace;
	
	private FeedbackGenerator feedbackGenerator;
	
	private AutoFeedbackMethods selectedMethod;
	
	final private int sleepTime = 500;
	
	public AutoDebugSimulator(Trace trace, AutoFeedbackMethods method) {
		this.trace = trace;
		this.selectedMethod = method;
		this.feedbackGenerator = FeedbackGenerator.getFeedbackGenerator(trace, selectedMethod);
	}
	
	/**
	 * Begin the auto debugging process. It will update the trace view and the feedback view as the debugging process.
	 */
	public void simulateDebugProcess() {
		
		// Begin with the latest node
		TraceNode currentNode = this.trace.getLatestNode();
		TraceNode prevNode = null;
		TraceNode rootCauseNode = null;
		
		// Skip the possible last node with only a function ending bracket: "}"
		String code = currentNode.getCodeStatement();
		if(code.replaceAll("\\s+", "").equals("}")) {	// Remove the tabs for comparison. It is wrong when there is comments.
			int currentOrder = currentNode.getOrder();
			currentNode = this.trace.getTraceNode(currentOrder-1);
		}
		
		StepRecommender recommander = new StepRecommender(Settings.enableLoopInference);
		boolean rootCauseFound = false;
		
		// Temporary implementation for baseline
		// Baseline will directly return the error trace node
		if(this.selectedMethod == AutoFeedbackMethods.BASELINE) {
			BaselineFeedbackGenerator baselineFeedbackGenerator = (BaselineFeedbackGenerator) this.feedbackGenerator;
			rootCauseNode = baselineFeedbackGenerator.getErrorNode();
		} else {
			
			// update trace view will erase the given initial feedback for NAIVE approach
			if(this.selectedMethod != AutoFeedbackMethods.NAIVE) {
				this.updateTraceView(currentNode);
			}
			
			while(!rootCauseFound) {
				// Note that the feedback can be null (when NAIVE method is selected)
				UserFeedback feedback = this.feedbackGenerator.giveFeedback(currentNode);
				if(feedback == null) {
					return;
				}
				
				// If the current node is correct, then it is very likely that there
				// is an incorrect operation in previous node
				if (feedback.getFeedbackType() == UserFeedback.CORRECT) {
					rootCauseNode = prevNode;
					rootCauseFound = true;
					break;
				}
				
				System.out.println("Current Node: " + currentNode.getOrder() + " with feedback: " + feedback.getFeedbackType());
				System.out.println("----------------------------------------");
				this.updateFeedbackView(feedback);
				
				TraceNode supiousNode = recommander.recommendNode(this.trace, currentNode, feedback);
				if (supiousNode == null) {
					rootCauseNode = currentNode;
					rootCauseFound = true;
					break;
				}
				
				this.updateTraceView(supiousNode);
				
				prevNode = currentNode;
				currentNode = supiousNode;
				
				if(prevNode == currentNode) {
					rootCauseNode = currentNode;
					rootCauseFound = true;
				}
			}
		}
		
		this.feedbackGenerator.notifyEnd();
		this.updateTraceView(rootCauseNode);
		System.out.println("The root cause is found to be " + rootCauseNode.getOrder());
		return;
	}
	
	private void updateFeedbackView(UserFeedback feedback) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();
				feedbackView.updateFeedbackView(feedback);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		});
	}
	
	/**
	 * Make trace view select the given node. It will sleep for half seconds for every update
	 * @param node The trace node to be focused
	 */
	private void updateTraceView(TraceNode node) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	TraceView traceView = MicroBatViews.getTraceView();
				traceView.jumpToNode(trace, node.getOrder(), true);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		});
	}
}
