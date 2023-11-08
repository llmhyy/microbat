package microbat.debugpilot.pathfinding;

import microbat.debugpilot.propagation.PropagatorFactory;
import microbat.debugpilot.settings.PathFinderSettings;
import microbat.log.Log;

public class PathFinderFactory {
	
	private PathFinderFactory() {}
	
	public static PathFinder getFinder(PathFinderSettings settings) {
		switch(settings.getPathFinderType()) {
		case CorrectnessDijkstra:
			return new CorrectnessDijkstraPathFinder(settings);
		case CorrectnessDijstraExp:
			return new CorrectnessDijkstraExpPathFinder(settings);
		case CorrectnessGreedy:
			return new CorrectnessGreedyPathFinder(settings);
		case Random:
			return new RandomPathFinder(settings);
		case SuspiciousDijkstra:
			return new SuspiciousDijkstraPathFinder(settings);
		case SuspiciousDijkstraExp:
			return new SuspiciousDijkstraExpPathFinder(settings);
		case SuspiciousGreedy:
			return new SuspiciousGreedyPathFinder(settings);
		default:
			throw new RuntimeException(Log.genMsg(PropagatorFactory.class, "Undefined path finder type: " + settings.getPathFinderType()));
		
		}
	}
}
