package microbat.algorithm.graphdiff;

import java.util.ArrayList;
import java.util.List;

import microbat.model.value.GraphNode;

public class SimpleMatcher implements Matcher{
	private List<GraphNode> visitedPool = new ArrayList<>();
	
	/**
	 * find a matchable node in <code>childrenAfter</code>
	 */
	private GraphNode findTheBestMatch(GraphNode childBefore, List<? extends GraphNode> childrenAfter){
		GraphNode node = null;
		for(GraphNode childAfter: childrenAfter){
			if(!isVisited(childAfter) && childBefore.match(childAfter)){
				return childAfter;
//				if(node == null){
//					node = childAfter;					
//				}
//				else if(childBefore.getStringValue().equals(childAfter.getStringValue())){
//					node = childAfter;
//				}
			}
		}
		
		return node;
	}
	
	@Override
	public List<MatchingGraphPair> matchList(List<? extends GraphNode> childrenBefore,
			List<? extends GraphNode> childrenAfter) {
		List<MatchingGraphPair> pairs = new ArrayList<>();
		
		for(GraphNode childBefore: childrenBefore){
			if(!isVisited(childBefore)){
				
				GraphNode node = findTheBestMatch(childBefore, childrenAfter);
				System.currentTimeMillis();
				
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
}
