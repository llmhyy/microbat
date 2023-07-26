package microbat.debugpilot.rootcausefinder;

import microbat.model.trace.TraceNode;

@FunctionalInterface
public interface RootCauseLocator {
	public TraceNode locateRootCause();
}
