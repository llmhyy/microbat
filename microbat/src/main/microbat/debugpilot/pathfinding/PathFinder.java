package microbat.debugpilot.pathfinding;

import microbat.model.trace.TraceNode;

public interface PathFinder {

	/**
	 * Construct a path from startNode to endNode. It will return null if no path can be constructed.
	 * @param startNode Start Node
	 * @param endNode Target Node
	 * @return A feedback path (consist of control and data dependency) from startNode to endNode
	 */
	public FeedbackPath findPath(final TraceNode startNode, final TraceNode endNode);
	
}