package microbat.autofeedback;

import java.util.ArrayList;
import java.util.List;

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
	
	private TraceView traceView;
	
	private List<NodeFeedbackPair> feedbacksRecord;
	
	final private int sleepTime = 1000;
	
	public AutoDebugSimulator(TraceView traceView, AutoFeedbackMethods method) {
		this.trace = traceView.getTrace();
		this.traceView = traceView;
		this.selectedMethod = method;
		this.feedbackGenerator = FeedbackGenerator.getFeedbackGenerator(trace, selectedMethod);
		this.feedbacksRecord = new ArrayList<>();
	}
	
	/**
	 * Begin the auto debugging process. It will update the trace view and the feedback view as the debugging process.
	 * @return Root cause node. Can be null.
	 */
	public TraceNode simulateDebugProcess() {
		
		// Clear the feedback record
		this.feedbacksRecord.clear();
		
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
					return rootCauseNode;
				}
				
				this.feedbacksRecord.add(new NodeFeedbackPair(currentNode, feedback));
				
				// If the current node is correct, then it is very likely that root cause node
				// is the previous node
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
		return rootCauseNode;
	}
	
	public List<NodeFeedbackPair> getFeedbackRecord() {
		return this.feedbacksRecord;
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
