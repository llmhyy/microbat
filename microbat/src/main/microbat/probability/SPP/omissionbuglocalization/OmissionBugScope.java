package microbat.probability.SPP.omissionbuglocalization;

import microbat.model.trace.TraceNode;

public class OmissionBugScope {
	private TraceNode startNode;
	private TraceNode endNode;
	
	public OmissionBugScope(final TraceNode startNode, final TraceNode endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	public void setScope(final TraceNode startNode, final TraceNode endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	public TraceNode getStartNode() {
		return this.startNode;
	}
	
	public TraceNode getEndNode() {
		return this.endNode;
	}
	
	public void updateStartNode(final TraceNode startNode) {
		this.startNode = startNode;
	}
	
	public void updateEndNode(final TraceNode endNode) {
		this.endNode = endNode;
	}
	
	public boolean contains(final TraceNode node) {
		final int order = node.getOrder();
		return order >= this.startNode.getOrder() && order <= this.endNode.getOrder(); 
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (otherObj instanceof OmissionBugScope) {
			OmissionBugScope otherScope = (OmissionBugScope) otherObj;
			if (!otherScope.startNode.equals(this.startNode)) {
				return false;
			}
			if (!otherScope.endNode.equals(this.endNode)) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + startNode.getOrder() + "," + endNode.getOrder() +"]";
	}
}
