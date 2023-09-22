package microbat.debugpilot.settings;

import java.util.List;

import microbat.debugpilot.pathfinding.PathFinderType;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class PathFinderSettings {
	
	public static final PathFinderType DEFAULT_PATH_FINDER_TYPE = PathFinderType.SuspiciousDijkstraExp;
	
	protected PathFinderType pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
	protected Trace trace = null;
	protected List<TraceNode> slicedTrace = null;
	
	public PathFinderSettings() {
		
	}

	public PathFinderType getPathFinderType() {
		return pathFinderType;
	}

	public void setPathFinderType(PathFinderType pathFinderType) {
		this.pathFinderType = pathFinderType;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public List<TraceNode> getSlicedTrace() {
		return slicedTrace;
	}

	public void setSlicedTrace(List<TraceNode> slicedTrace) {
		this.slicedTrace = slicedTrace;
	}


}
