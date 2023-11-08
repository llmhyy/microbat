package microbat.debugpilot.pathfinding;

import java.util.List;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class CorrectnessDijkstraPathFinder extends DijkstraPathFinder {

	public CorrectnessDijkstraPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public CorrectnessDijkstraPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}

	public CorrectnessDijkstraPathFinder() {
		super();
	}

	@Override
	protected double getCost(VarValue varValue) {
		return varValue.getCorrectness();
	}
}
