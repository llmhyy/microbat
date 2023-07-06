package microbat.probability.SPP.pathfinding;

import java.util.List;

import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.probability.SPP.propagation.PropagatorFactory;

public class PathFinderFactory {
	
	private PathFinderFactory() {}
	
	public static PathFinder getFinder(final PathFinderType type, final Trace trace, final List<TraceNode> slicedTrace) {
		switch(type) {
		case Dijstra:
			return new DijstraPathFinder(trace, slicedTrace);
		case Greedy:
			return new GreedyPathFinder(trace, slicedTrace);
		case Random:
			return new RandomPathFinder(trace, slicedTrace);
		default:
			throw new RuntimeException(Log.genMsg(PropagatorFactory.class, "Undefined path finder type: " + type));
		}
	}
}
