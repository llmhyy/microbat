package microbat.handler;

import microbat.Activator;
import microbat.debugpilot.pathfinding.PathFinderType;
import microbat.debugpilot.propagation.PropagatorType;
import microbat.debugpilot.rootcausefinder.RootCauseLocatorType;
import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.debugpilot.settings.RootCauseLocatorSettings;
import microbat.preference.DebugPilotPreference;

public class PreferenceParser {
	
	public static PropagatorSettings getPreferencePropagatorSettings() {
		PropagatorSettings settings = new PropagatorSettings();
		settings.setPropagatorType(PreferenceParser.getPreferencePropagatorType());
//		settings.setUseLocalServer(PreferenceParser.getPrferenceUseLocalServer());
//		settings.setServerHost(PreferenceParser.getPreferenceServerHost());
//		settings.setServerPort(PreferenceParser.getPreferenceServerPort());
		return settings;
	}
	
	public static PathFinderSettings getPreferencePathFinderSettings() {
		PathFinderSettings settings = new PathFinderSettings();
		settings.setPathFinderType(PreferenceParser.getPreferencePathFinderType());
		return settings;
	}
	
	public static RootCauseLocatorSettings getPrefereRootCauseLocatorSettings() {
		RootCauseLocatorSettings settings = new RootCauseLocatorSettings();
		settings.setRootCauseLocatorType(PreferenceParser.getPreferenceRootCauseLocatorType());
		return settings;
	}
	
	public static PropagatorType getPreferencePropagatorType() {
		final String propagatorTypeString = PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.PROPAGATOR_TYPE_KEY);
		return propagatorTypeString == null  ? PropagatorSettings.DEFAULT_PROPAGATOR_TYPE : PropagatorType.valueOf(propagatorTypeString);
	}
	
//	public static boolean getPrferenceUseLocalServer() {
//		final String string = PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.USE_LOCAL_SERVER_KEY);
//		return string == null ? PropagatorSettings.DEFAULT_USE_LOCATL_SERVER : Boolean.valueOf(string);
//	}
//	
//	public static String getPreferenceServerHost() {
//		final String string =  PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.SERVER_HOST_KEY);
//		return string == null ? PropagatorSettings.DEFAULT_SERVER_HOST : string;
//	}
//	
//	public static int getPreferenceServerPort() {
//		final String string =  PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.SERVER_PORT_KEY);
//		return string == null ? PropagatorSettings.DEFAULT_SERVER_PORT : Integer.valueOf(string);
//	}
	
	public static PathFinderType getPreferencePathFinderType() {
		final String string = PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.PATHFINDER_TYPE_KEY);
		return string == null ? PathFinderSettings.DEFAULT_PATH_FINDER_TYPE : PathFinderType.valueOf(string);
	}
	
	public static RootCauseLocatorType getPreferenceRootCauseLocatorType() {
		final String string = PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.ROOT_CAUSE_LOCATOR_TYPE_KEY);
		return string == null ? RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE : RootCauseLocatorType.valueOf(string);
	}
	
//	public static String getLogPath() {
//		final String string = PreferenceParser.getPreferenceStringByKey(DebugPilotPreference.LOG_PATH_KEY);
//		return string == null ? DebugPilotPreference.defaultLogPath : string;
//	}

	public static String getPreferenceStringByKey(final String key) {
		final String string = Activator.getDefault().getPreferenceStore().getString(key);
		return string.isEmpty() ? null : string;
	}
}
