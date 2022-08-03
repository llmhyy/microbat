package microbat.baseline.evaluator;

import java.util.ArrayList;
import java.util.List;

import microbat.baseline.encoders.NodeFeedbackPair;
import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class BaselineEvaluator {
	
	private Trace buggyTrace;
	private Trace correctTrace;
	
	private ProbabilityEncoder encoder;
	
	private final double exporeRatio = 0.5;
	
	public BaselineEvaluator(Trace buggyTrace, Trace correcTrace) {
		this.buggyTrace = buggyTrace;
		this.correctTrace = correctTrace;
		
		this.encoder = new ProbabilityEncoder(buggyTrace);
	}
	
	/**
	 * Evaluate the performance of the baseline approach.
	 * Evaluation matrices include the number of feedback needed to reach the root cause
	 * 
	 * @return Number of feedback needed to reach the root cause
	 */
	public int evaluate() {
		
		final int maxFeedbackLimit = (int) Math.floor(this.buggyTrace.getExecutionList().size()/this.exporeRatio);
		
		final List<NodeFeedbackPair> feedbacks = new ArrayList<>();
		
		int rootCause = 0;
		
		TraceNode prediction = this.encoder.getMostErroneousNode();
		while (prediction.getOrder() != rootCause && feedbacks.size() <= maxFeedbackLimit) {
			
		}
		
		return feedbacks.size();
	}
	
}
