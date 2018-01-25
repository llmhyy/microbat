package microbat.algorithm.graphdiff;

import microbat.model.value.GraphNode;

public class MatchingGraphPair{
	private GraphNode nodeBefore;
	private GraphNode nodeAfter;
	
	public MatchingGraphPair(GraphNode nodeBefore, GraphNode nodeAfter) {
		super();
		this.nodeBefore = nodeBefore;
		this.nodeAfter = nodeAfter;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		String diffType = getDiffType();
		buffer.append(diffType + ": ");
		if(this.nodeBefore != null){
			buffer.append(this.nodeBefore.toString());
		}
		if(diffType.equals(GraphDiff.UPDATE)){
			buffer.append(" -> ");
		}
		if(this.nodeAfter != null){
			buffer.append(this.nodeAfter.toString());
		}
		
		return buffer.toString();
	}
	
	public String getDiffType(){
		if(this.nodeBefore == null && this.nodeAfter != null){
			return GraphDiff.ADD;
		}
		else if(this.nodeBefore != null && this.nodeAfter == null){
			return GraphDiff.REMOVE;
		}
		else if(this.nodeBefore != null && this.nodeAfter != null){
			return GraphDiff.UPDATE;
		}
		else{
			System.err.println("both before-node and after-node are empty for a change!");
			return null;
		}
	}
	
	public GraphNode getNodeBefore() {
		return nodeBefore;
	}
	public void setNodeBefore(GraphNode nodeBefore) {
		this.nodeBefore = nodeBefore;
	}
	public GraphNode getNodeAfter() {
		return nodeAfter;
	}
	public void setNodeAfter(GraphNode nodeAfter) {
		this.nodeAfter = nodeAfter;
	}

	
}