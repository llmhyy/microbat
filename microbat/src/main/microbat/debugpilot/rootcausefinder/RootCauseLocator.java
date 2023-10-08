package microbat.debugpilot.rootcausefinder;

import microbat.model.trace.TraceNode;

@FunctionalInterface
public interface RootCauseLocator {
	/**
	 * Locate the root cause
	 * @return Predicted root cause
	 */
	public TraceNode locateRootCause();
}
