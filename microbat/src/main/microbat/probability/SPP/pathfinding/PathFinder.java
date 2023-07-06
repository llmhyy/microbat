package microbat.probability.SPP.pathfinding;

import microbat.model.trace.TraceNode;

public interface PathFinder {

	public ActionPath findPath(final TraceNode startNode, final TraceNode endNode);
	
}