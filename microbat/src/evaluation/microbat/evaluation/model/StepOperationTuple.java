package microbat.evaluation.model;

import microbat.model.trace.TraceNode;
import microbat.recommendation.DebugState;
import microbat.recommendation.UserFeedback;

public class StepOperationTuple {
	private TraceNode node;
	private UserFeedback userFeedback;
	
	/**
	 * the corresponding node in correct trace.
	 */
	private TraceNode referenceNode;
	
	/**
	 * the debugging state before the user feedback
	 */
	private int debugState;
	
	public StepOperationTuple(TraceNode node, UserFeedback userFeedback, TraceNode referenceNode, int debugState) {
		super();
		this.node = node;
		this.userFeedback = userFeedback;
		this.referenceNode = referenceNode;
		this.debugState = debugState;
	}
	
	public TraceNode getNode() {
		return node;
	}
	public void setNode(TraceNode node) {
		this.node = node;
	}
	public UserFeedback getUserFeedback() {
		return userFeedback;
	}
	public void setUserFeedback(UserFeedback userFeedback) {
		this.userFeedback = userFeedback;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("StepOperationTuple [node=" + node + ", userFeedback=" + userFeedback);
		
		String state = DebugState.printState(this.debugState);
		buffer.append(", state=" + state);
		
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

	public int getDebugState() {
		return debugState;
	}

	public void setDebugState(int debugState) {
		this.debugState = debugState;
	}
	
	
}
