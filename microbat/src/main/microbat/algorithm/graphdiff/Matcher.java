package microbat.algorithm.graphdiff;

import java.util.List;

import microbat.model.value.GraphNode;

/**
 * This class is to indicate how to match two child lists of two matched parent. 
 * @author Yun Lin
 *
 */
public interface Matcher {
	public List<MatchingGraphPair> matchList(List<? extends GraphNode> childrenBefore,
			List<? extends GraphNode> childrenAfter);
}
