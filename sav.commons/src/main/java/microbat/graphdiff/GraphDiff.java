package microbat.graphdiff;

import sav.strategies.dto.execute.value.GraphNode;

public class GraphDiff { 
	public static final String ADD = "add";
	public static final String REMOVE = "remove";
	public static final String UPDATE = "update";
	
	private GraphNode nodeBefore;
	private GraphNode nodeAfter;
	
	public GraphDiff(GraphNode nodeBefore, GraphNode nodeAfter) {
		super();
		
		if(nodeBefore == null && nodeAfter == null){
			System.err.println("both before-node and after-node are empty!");
		}
			
		this.nodeBefore = nodeBefore;
		this.nodeAfter = nodeAfter;
		
	}
	
	public GraphNode getChangedNode(){
		GraphNode node = getNodeAfter();
		if(node == null){
			node = getNodeBefore();
		}
		
		return node;
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
