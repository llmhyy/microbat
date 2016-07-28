package microbat.evaluation.model;

import microbat.model.trace.TraceNode;

public class StepOperationTuple {
	private TraceNode node;
	private String userFeedback;
	/**
	 * the corresponding node in correct trace.
	 */
	private TraceNode referenceNode;
	
	public StepOperationTuple(TraceNode node, String userFeedback, TraceNode referenceNode) {
		super();
		this.node = node;
		this.userFeedback = userFeedback;
		this.referenceNode = referenceNode;
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
		StringBuffer buffer = new StringBuffer();
		buffer.append("StepOperationTuple [node=" + node + ", userFeedback=" + userFeedback);
		if(getReferenceNode() != null){
			buffer.append(", reference node=" + getReferenceNode());
		}
		buffer.append("]");
		return buffer.toString();
	}

	public TraceNode getReferenceNode() {
		return referenceNode;
	}

	public void setReferenceNode(TraceNode referenceNode) {
		this.referenceNode = referenceNode;
	}
	
	
}
