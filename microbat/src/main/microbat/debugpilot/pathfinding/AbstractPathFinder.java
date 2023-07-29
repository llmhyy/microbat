package microbat.debugpilot.pathfinding;

import java.util.List;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public abstract class AbstractPathFinder implements PathFinder {
	
	protected final Trace trace;
	
	protected final List<TraceNode> slicedTrace;
	
	public AbstractPathFinder(final PathFinderSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace());
	}
	
	public AbstractPathFinder(Trace trace, final List<TraceNode> slicedTrace ) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
	}
	
	@Override
	public abstract FeedbackPath findPath(TraceNode startNode, TraceNode endNode);

}
