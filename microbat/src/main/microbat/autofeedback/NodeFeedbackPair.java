package microbat.autofeedback;

import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;

/**
 * Pair of trace node and the corresponding user feedback
 * @author David
 */
public class NodeFeedbackPair {
	private TraceNode node;
	private UserFeedback feedback;
	
	public NodeFeedbackPair() {
		this.node = null;
		this.feedback = null;
	}
	
	public NodeFeedbackPair(TraceNode node, UserFeedback feedback) {
		this.node = node;
		this.feedback = feedback;
	}
	
	public TraceNode getNode() {
		return this.node;
	}
	
	public UserFeedback getFeedback() {
		return this.feedback;
	}
	
	@Override
	public String toString() {
		return "Node Order: " + this.node.getOrder() +
			   " Feedback: " + this.feedback;
	}
}
