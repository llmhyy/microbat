package microbat.probability.SPP.pathfinding;

import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public abstract class AbstractPathFinder implements PathFinder {
	
	protected final Trace trace;
	
	protected final List<TraceNode> slicedTrace;
	
	public AbstractPathFinder(Trace trace, final List<TraceNode> slicedTrace ) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
	}
	
	@Override
	public abstract ActionPath findPath(TraceNode startNode, TraceNode endNode);

}
