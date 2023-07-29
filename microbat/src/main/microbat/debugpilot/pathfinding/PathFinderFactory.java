package microbat.debugpilot.pathfinding;

import microbat.debugpilot.propagation.PropagatorFactory;
import microbat.debugpilot.settings.PathFinderSettings;
import microbat.log.Log;

public class PathFinderFactory {
	
	private PathFinderFactory() {}
	
	public static PathFinder getFinder(PathFinderSettings settings) {
		switch(settings.getPathFinderType()) {
		case Dijkstra:
			return new DijkstraPathFinder(settings);
		case Greedy:
			return new GreedyPathFinder(settings);
		case Random:
			return new RandomPathFinder(settings);
		case DijkstraExp:
			return new DijkstraExpPathFinder(settings);
		default:
			throw new RuntimeException(Log.genMsg(PropagatorFactory.class, "Undefined path finder type: " + settings.getPathFinderType()));
		}
	}
}
