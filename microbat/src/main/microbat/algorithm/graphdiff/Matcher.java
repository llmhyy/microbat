package microbat.algorithm.graphdiff;

import java.util.List;

import microbat.model.value.GraphNode;

public interface Matcher {
	public List<MatchingGraphPair> matchList(List<? extends GraphNode> childrenBefore,
			List<? extends GraphNode> childrenAfter);
}
