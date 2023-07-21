package microbat.debugpilot.pathfinding;

import java.util.List;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class DijkstraExpPathFinder extends DijkstraPathFinder {

	public DijkstraExpPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}

	@Override
	public FeedbackPath findPath(TraceNode startNode, TraceNode endNode) {
		FeedbackPath explanablePath = super.findPath(startNode, endNode);
		for (int pathIdx=0; pathIdx < explanablePath.getLength(); pathIdx++) {
			final NodeFeedbacksPair pair = explanablePath.get(pathIdx);
			final TraceNode node = pair.getNode();
			final UserFeedback greedyFeedback = GreedyPathFinder.giveFeedback(node);
			if (!pair.containsFeedback(greedyFeedback)) {
				final TraceNode nextNode = TraceUtil.findNextNode(node, greedyFeedback, trace);
				if (nextNode != null) {
					
				}
			}
		}
		return explanablePath;
	}

}
