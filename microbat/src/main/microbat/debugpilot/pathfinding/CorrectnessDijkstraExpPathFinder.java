package microbat.debugpilot.pathfinding;

import java.util.List;
import java.util.Objects;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.settings.PathFinderSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class CorrectnessDijkstraExpPathFinder extends CorrectnessDijkstraPathFinder {

	public CorrectnessDijkstraExpPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public CorrectnessDijkstraExpPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}

	@Override
	public FeedbackPath findPath(TraceNode startNode, TraceNode endNode) {
		Objects.requireNonNull(startNode, Log.genMsg(getClass(), "Start node should not be null"));
		Objects.requireNonNull(endNode, Log.genMsg(getClass(), "End node should not be null"));
		
		// Try to construct path with greedy selection
		FeedbackPath explanablePath = super.findPath(startNode, endNode);
		if (explanablePath == null) 
			return null;

		for (int pathIdx=0; pathIdx < explanablePath.getLength(); pathIdx++) {
			final NodeFeedbacksPair pair = explanablePath.get(pathIdx);
			final TraceNode node = pair.getNode();
			final UserFeedback greedyFeedback = CorrectnessGreedyPathFinder.giveFeedback_static(node);
			if (!pair.containsFeedback(greedyFeedback)) {
				final TraceNode nextNode = TraceUtil.findNextNode(node, greedyFeedback, trace);
				if (nextNode != null) {
					 FeedbackPath insertPath = super.findPath(nextNode, endNode);
					 if (insertPath != null) {
						 explanablePath.get(pathIdx).setFeedback(greedyFeedback);
						 explanablePath = FeedbackPathUtil.splicePathAtIndex(explanablePath, insertPath, pathIdx+1);
					 }
				}
			}
		}
		return explanablePath;
	}

}
