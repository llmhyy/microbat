package microbat.debugpilot.pathfinding;

import java.util.List;

import microbat.debugpilot.propagation.PropagatorFactory;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class PathFinderFactory {
	
	private PathFinderFactory() {}
	
	public static PathFinder getFinder(final PathFinderType type, final Trace trace, final List<TraceNode> slicedTrace) {
		switch(type) {
		case Dijkstra:
			return new DijkstraPathFinder(trace, slicedTrace);
		case Greedy:
			return new GreedyPathFinder(trace, slicedTrace);
		case Random:
			return new RandomPathFinder(trace, slicedTrace);
		case DijkstraExp:
			return new DijkstraExpPathFinder(trace, slicedTrace);
		default:
			throw new RuntimeException(Log.genMsg(PropagatorFactory.class, "Undefined path finder type: " + type));
		}
	}
}
