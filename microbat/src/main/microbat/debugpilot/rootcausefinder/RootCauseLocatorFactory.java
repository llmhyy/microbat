package microbat.debugpilot.rootcausefinder;

import microbat.debugpilot.settings.RootCauseLocatorSettings;
import microbat.log.Log;

public class RootCauseLocatorFactory {
	
	private RootCauseLocatorFactory() {}
	
	public static RootCauseLocator getLocator(RootCauseLocatorSettings settings) {
		switch (settings.getRootCauseLocatorType()) {
		case PROBINFER:
			return new ProbInferRootCauseLocator(settings);
		case SPP:
			return new SPPRootCauseLocator(settings);
		case SUSPICIOUS:
			return new SuspiciousRootCauseLocator(settings);
		default:
			throw new IllegalArgumentException(Log.genMsg("RootCauseLocatorFactory", "Unhandled root casue locator type" + settings.getRootCauseLocatorType()));
		}
	}
}
