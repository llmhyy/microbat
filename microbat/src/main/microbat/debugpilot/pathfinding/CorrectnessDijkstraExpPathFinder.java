package microbat.debugpilot.pathfinding;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
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

		for (int pathIdx=0; pathIdx < explanablePath.length(); pathIdx++) {
			final DPUserFeedback feedback = explanablePath.get(pathIdx);
			final TraceNode node = feedback.getNode();
			final DPUserFeedback greedyFeedback = CorrectnessGreedyPathFinder.giveFeedback_static(node);
			if (!feedback.isSimilar(greedyFeedback)) {
				// If the current feedback is not the greedy one,
				// then try to construct a new shortest path with this greedy feedback
				Set<TraceNode> nextNodes = TraceUtil.findAllNextNodes(greedyFeedback);
				for (TraceNode nextNode : nextNodes) {
					FeedbackPath insertPath = super.findPath(nextNode, endNode);
					if (insertPath != null) {
						explanablePath.set(pathIdx, greedyFeedback);
						explanablePath = FeedbackPath.splicePathAtIndex(explanablePath, insertPath, pathIdx+1);
					}
				}
			}
		}
		return explanablePath;
	}

}
