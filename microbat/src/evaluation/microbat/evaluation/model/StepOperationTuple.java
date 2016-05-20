package microbat.evaluation.model;

import microbat.model.trace.TraceNode;

public class StepOperationTuple {
	private TraceNode node;
	private String userFeedback;
	
	
	public StepOperationTuple(TraceNode node, String userFeedback) {
		super();
		this.node = node;
		this.userFeedback = userFeedback;
	}
	
	public TraceNode getNode() {
		return node;
	}
	public void setNode(TraceNode node) {
		this.node = node;
	}
	public String getUserFeedback() {
		return userFeedback;
	}
	public void setUserFeedback(String userFeedback) {
		this.userFeedback = userFeedback;
	}

	@Override
	public String toString() {
		return "StepOperationTuple [node=" + node + ", userFeedback="
				+ userFeedback + "]";
	}
	
	
}
