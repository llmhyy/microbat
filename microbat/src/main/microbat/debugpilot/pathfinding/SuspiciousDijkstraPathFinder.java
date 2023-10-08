package microbat.debugpilot.pathfinding;

import java.util.List;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SuspiciousDijkstraPathFinder extends CorrectnessDijkstraPathFinder {
	
	protected static final double eps = 1e-7;
	
	public SuspiciousDijkstraPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public SuspiciousDijkstraPathFinder(Trace trace, List<TraceNode> slicedTrace) {
		super(trace, slicedTrace);
	}
	
	public SuspiciousDijkstraPathFinder() {
		super();
	}
	
	@Override
	protected double getCost(final VarValue varValue) {
		return 1.0d / (varValue.getSuspiciousness() + SuspiciousDijkstraPathFinder.eps); 
	}
}
