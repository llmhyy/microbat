package microbat.algorithm.graphdiff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import microbat.model.value.GraphNode;

/**
 * This class is created for improving the efficiency of variable comparison. It assumes 
 * that both the childrenBefore and the childrenAfter are sorted.
 * 
 * @author Yun Lin
 *
 */
public class SortedGraphMatcher implements Matcher {

	private Comparator<GraphNode> comparator;
	
	public SortedGraphMatcher(Comparator<GraphNode> comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public List<MatchingGraphPair> matchList(List<? extends GraphNode> childrenBefore,
			List<? extends GraphNode> childrenAfter) {
		List<MatchingGraphPair> pairs = new ArrayList<>();
		
		int beforeCursor = 0;
		int afterCursor = 0;
		
		while(beforeCursor < childrenBefore.size() && afterCursor < childrenAfter.size()){
			GraphNode nodeBefore = childrenBefore.get(beforeCursor);
			GraphNode nodeAfter = childrenAfter.get(afterCursor);
			
			if(nodeBefore.match(nodeAfter)){
				MatchingGraphPair pair = new MatchingGraphPair(nodeBefore, nodeAfter);
				pairs.add(pair);
				beforeCursor++;
				afterCursor++;
			}
			else{
				int result = getComparator().compare(nodeBefore, nodeAfter);
				if(result < 0){
					MatchingGraphPair pair = new MatchingGraphPair(nodeBefore, null);
					pairs.add(pair);
					beforeCursor++;
				}
				else{
					MatchingGraphPair pair = new MatchingGraphPair(null, nodeAfter);
					pairs.add(pair);
					afterCursor++;
				}
				
			}
		}
		
		if(beforeCursor < childrenBefore.size()-1){
			for(int i=beforeCursor; i<childrenBefore.size(); i++){
				GraphNode nodeBefore = childrenBefore.get(i);
				MatchingGraphPair pair = new MatchingGraphPair(nodeBefore, null);
				pairs.add(pair);
			}
		}
		else if(afterCursor < childrenAfter.size()-1){
			for(int i=afterCursor; i<childrenAfter.size(); i++){
				GraphNode nodeAfter = childrenAfter.get(i);
				MatchingGraphPair pair = new MatchingGraphPair(nodeAfter, null);
				pairs.add(pair);
			}
		}
		
		return pairs;
	}

	public Comparator<GraphNode> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<GraphNode> comparator) {
		this.comparator = comparator;
	}

}
