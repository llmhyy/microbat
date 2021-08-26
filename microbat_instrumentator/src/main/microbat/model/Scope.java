package microbat.model;

import microbat.model.trace.TraceNode;

public interface Scope {
	public boolean containsNodeScope(TraceNode node);
	public boolean containLocation(ClassLocation location);
	public boolean isLoop();
}
