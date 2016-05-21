package microbat.algorithm.graphdiff;

import java.util.ArrayList;
import java.util.List;

import microbat.model.value.GraphNode;

public class HierarchyGraphDiffer {
	private Matcher matcher = new SimpleMatcher();
	private List<GraphDiff> diffs = new ArrayList<>();
	
	/**
	 * This diff result does not contain the difference of rootBefore and rootAfter themselves, only the
	 * different of their children.
	 * <p>
	 * The isCompareRoot parameter is to indicate whether the algorithm need to consider the difference of
	 * the root node.
	 * 
	 * @param rootBefore
	 * @param rootAfter
	 * @param isCompareRoot
	 */
	public void diff(GraphNode rootBefore, GraphNode rootAfter, boolean isCompareRoot){
		if(isCompareRoot){
			if(!rootBefore.isTheSameWith(rootAfter)){
				GraphDiff diff = new GraphDiff(rootBefore, rootAfter);
				this.diffs.add(diff);
			}
		}
		
		diffChildren(rootBefore, rootAfter);
	}

	private void diffChildren(GraphNode rootBefore, GraphNode rootAfter) {
		List<? extends GraphNode> childrenBefore = rootBefore.getChildren();
		List<? extends GraphNode> childrenAfter = rootAfter.getChildren();
		List<MatchingGraphPair> pairs = matcher.matchList(childrenBefore, childrenAfter);

		for(MatchingGraphPair pair: pairs){
			GraphNode nodeBefore = pair.getNodeBefore();
			GraphNode nodeAfter = pair.getNodeAfter();
			
			if(nodeBefore != null && nodeAfter != null){
				if(!nodeBefore.isTheSameWith(nodeAfter)){
					GraphDiff diff = new GraphDiff(nodeBefore, nodeAfter);
					this.diffs.add(diff);
				}
				
				diffChildren(nodeBefore, nodeAfter);
			}
			else{
				GraphDiff diff = new GraphDiff(nodeBefore, nodeAfter);
				this.diffs.add(diff);
			}
		}
		
	}
	
	public List<GraphDiff> getDiffs(){
		return this.diffs;
	}
}
