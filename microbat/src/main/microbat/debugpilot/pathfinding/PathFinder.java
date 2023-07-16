package microbat.debugpilot.pathfinding;

import microbat.model.trace.TraceNode;

public interface PathFinder {

	public FeedbackPath findPath(final TraceNode startNode, final TraceNode endNode);
	
}