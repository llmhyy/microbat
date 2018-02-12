package microbat.graphdiff;

import java.util.ArrayList;
import java.util.List;

import sav.strategies.dto.execute.value.GraphNode;

public class HierarchyGraphDiffer {
	
	private List<GraphNode> visitedPool = new ArrayList<>();
	
	class MatchingGraphPair{
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
	
	private List<GraphDiff> diffs = new ArrayList<>();
	
	public void diff(GraphNode rootBefore, GraphNode rootAfter){
		List<? extends GraphNode> childrenBefore = rootBefore.getChildren();
		List<? extends GraphNode> childrenAfter = rootAfter.getChildren();
		
		List<MatchingGraphPair> pairs = matchList(childrenBefore, childrenAfter);
		
		for(MatchingGraphPair pair: pairs){
			GraphNode nodeBefore = pair.getNodeBefore();
			GraphNode nodeAfter = pair.getNodeAfter();
			
			if(nodeBefore != null && nodeAfter != null){
				if(!nodeBefore.isTheSameWith(nodeAfter)){
					GraphDiff diff = new GraphDiff(nodeBefore, nodeAfter);
					this.diffs.add(diff);
				}
				
				diff(nodeBefore, nodeAfter);
			}
			else{
				GraphDiff diff = new GraphDiff(nodeBefore, nodeAfter);
				this.diffs.add(diff);
			}
		}
	}

	private List<MatchingGraphPair> matchList(List<? extends GraphNode> childrenBefore,
			List<? extends GraphNode> childrenAfter) {
		List<MatchingGraphPair> pairs = new ArrayList<>();
		
		for(GraphNode childBefore: childrenBefore){
			if(!isVisited(childBefore)){
				/**
				 * find a matchable node in <code>childrenAfter</code>
				 */
				GraphNode node = null;
				for(GraphNode childAfter: childrenAfter){
					if(!isVisited(childAfter) && childBefore.match(childAfter)){
						node = childAfter;
						break;
					}
				}
				//System.currentTimeMillis();
				
				setVisited(childBefore);
				if(node != null){
					setVisited(node);
				}
				MatchingGraphPair pair = new MatchingGraphPair(childBefore, node);
				pairs.add(pair);
			}
		}
		
		
		for(GraphNode childAfter: childrenAfter){
			if(!isVisited(childAfter)){
				setVisited(childAfter);
				MatchingGraphPair pair = new MatchingGraphPair(null, childAfter);
				pairs.add(pair);
			}
		}
		
		return pairs;
	}
	
	private boolean isVisited(GraphNode node){
		return this.visitedPool.contains(node);
	}
	
	private void setVisited(GraphNode node){
		this.visitedPool.add(node);
	}
	
	public List<GraphDiff> getDiffs(){
		return this.diffs;
	}
}
