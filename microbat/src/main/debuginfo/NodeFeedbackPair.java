package debuginfo;

import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;

/**
 * Pair of node and correspondence user feedback
 * @author David
 *
 */
public class NodeFeedbackPair {
	
	private TraceNode node;
	private UserFeedback feedback;
	
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
	
	public void setNode(TraceNode node) {
		this.node = node;
	}
	
	public void setFeedback(UserFeedback feedback) {
		this.feedback = feedback;
	}
	
	public boolean reviewingSameNode(NodeFeedbackPair pair) {
		return this.getNode().getOrder() == pair.getNode().getOrder();
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (otherObj instanceof NodeFeedbackPair) {
			NodeFeedbackPair otherPair = (NodeFeedbackPair) otherObj;
			return otherPair.node.equals(this.node) && otherPair.feedback.equals(this.feedback);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.node + " with feedback " + this.feedback;
	}
}
