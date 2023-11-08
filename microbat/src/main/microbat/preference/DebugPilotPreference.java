package microbat.preference;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import microbat.Activator;
import microbat.debugpilot.pathfinding.PathFinderType;
import microbat.debugpilot.propagation.PropagatorType;
import microbat.debugpilot.rootcausefinder.RootCauseLocatorType;
import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.debugpilot.settings.RootCauseLocatorSettings;

public class DebugPilotPreference extends PreferencePage implements IWorkbenchPreferencePage  {

	private static final String ID = "microbat.preference.debugpilot";
	
	public static final String PROPAGATOR_TYPE_KEY = "DP/PROPAGATOR_TYPE";
	public static final String PATHFINDER_TYPE_KEY = "DP/PATHFINDER_TYPE";
	public static final String ROOT_CAUSE_LOCATOR_TYPE_KEY = "DP/ROOT_CAUSE_LOCATOR_TYPE";
//	public static final String LOG_PATH_KEY = "DP/LOG_PATH_KEY";
	
	/* Probability Propagation Settings */
	protected Combo propagatorTypeCombo;
	protected PropagatorType propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
	
	/* Path Finder Settings */
	protected Combo pathFinderTypeCombo;
	protected PathFinderType pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
	
	/* Root Cause Locator Settings */
	protected Combo rootCauseLocatorTypeCombo;
	protected RootCauseLocatorType rootCauseLocatorType = RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE;
	
//	protected Text logText;
//	public static String defaultLogPath = Paths.get("user_behavior_log.txt").toAbsolutePath().toString();
//	protected String logPath;
	
			
	public DebugPilotPreference() {
		super("Debug Pilot Settings");
		this.setDescription("Configuration for debug pilot");
	}
	
	@Override
	public void init(IWorkbench workbench) {
		try {			
			this.propagatorType = PropagatorType.valueOf(Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.PROPAGATOR_TYPE_KEY));
			this.pathFinderType = PathFinderType.valueOf(Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.PATHFINDER_TYPE_KEY));
			this.rootCauseLocatorType = RootCauseLocatorType.valueOf(Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.ROOT_CAUSE_LOCATOR_TYPE_KEY));
//			this.logPath = Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.LOG_PATH_KEY);
		} catch (IllegalArgumentException e) {
			this.propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
			this.pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
			this.rootCauseLocatorType = RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE;
//			this.logPath = this.defaultLogPath;
		}
		
//		if (this.logPath.isEmpty()) {
//			this.logPath = this.defaultLogPath;
//		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout());
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.createPropagatorSetting(contents);
		this.createPathFinderSetting(contents);
		this.createRootCauseLocatorSetting(contents);
//		this.createLogTextSetting(contents);
		return contents;
	}
	
	@Override
	public boolean performOk() {
		this.propagatorType = PropagatorType.valueOf(this.propagatorTypeCombo.getText());
		this.pathFinderType = PathFinderType.valueOf(this.pathFinderTypeCombo.getText());
		this.rootCauseLocatorType = RootCauseLocatorType.valueOf(this.rootCauseLocatorTypeCombo.getText());		
//		this.logPath = this.logText.getText();
		this.storeData();
		return true;
	}
	
	@Override
	public void performDefaults() {
		super.performDefaults();
		this.propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
		this.pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
		
		this.propagatorTypeCombo.select(this.propagatorType.ordinal());
		this.pathFinderTypeCombo.select(this.pathFinderType.ordinal());
		this.rootCauseLocatorTypeCombo.select(this.rootCauseLocatorType.ordinal());
//		this.logPath = this.logText.getText();
		this.storeData();
	}
	
	protected void storeData() {
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(DebugPilotPreference.ID);
		preferences.put(DebugPilotPreference.PROPAGATOR_TYPE_KEY, this.propagatorType.name());
		preferences.put(DebugPilotPreference.PATHFINDER_TYPE_KEY, this.pathFinderType.name());
		preferences.put(DebugPilotPreference.ROOT_CAUSE_LOCATOR_TYPE_KEY, this.rootCauseLocatorType.name());
//		preferences.put(DebugPilotPreference.LOG_PATH_KEY, this.logPath);
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.PROPAGATOR_TYPE_KEY, this.propagatorType.name());
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.PATHFINDER_TYPE_KEY, this.pathFinderType.name());
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.ROOT_CAUSE_LOCATOR_TYPE_KEY, this.rootCauseLocatorType.name());
//		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.LOG_PATH_KEY, this.logPath);
	}
	
	protected void createPropagatorSetting(final Composite parent) {
		final Group propagatorGroup = new Group(parent, SWT.NONE);
		propagatorGroup.setText("Probability Propagator Settings");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		propagatorGroup.setLayout(layout);
		
		propagatorGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label propagatorTypeLable = new Label(propagatorGroup, SWT.None);
		propagatorTypeLable.setText("Probability Propagation Method: ");
		
		this.propagatorTypeCombo = new Combo(propagatorGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		final String[] selectionsName = Stream.of(PropagatorType.values()).map(Enum::name).toArray(String[]::new);
		this.propagatorTypeCombo.setItems(selectionsName);
		this.propagatorTypeCombo.select(this.propagatorType.ordinal());
		
		GridData comboData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboData.horizontalAlignment = GridData.END;
		this.propagatorTypeCombo.setLayoutData(comboData);
	}
	
	protected void createPathFinderSetting(final Composite parent) {
		final Group pathFindingGroup = new Group(parent, SWT.NONE);
		pathFindingGroup.setText("Path Finding Settings");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		pathFindingGroup.setLayout(layout);
		
		pathFindingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label pathFinderTypeLabel = new Label(pathFindingGroup, SWT.NONE);
		pathFinderTypeLabel.setText("Path Finding Method");
		
		this.pathFinderTypeCombo = new Combo(pathFindingGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		final String[] selectionsName = Stream.of(PathFinderType.values()).map(Enum::name).toArray(String[]::new);
		this.pathFinderTypeCombo.setItems(selectionsName);
		this.pathFinderTypeCombo.select(this.pathFinderType.ordinal());
		
		GridData comboData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboData.horizontalAlignment = GridData.END;
		this.pathFinderTypeCombo.setLayoutData(comboData);
	}
	
	protected void createRootCauseLocatorSetting(final Composite parent) {
		final Group rootCauseGroup = new Group(parent, SWT.NONE);
		rootCauseGroup.setText("Root Cause Locator Settings");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		rootCauseGroup.setLayout(layout);
		
		rootCauseGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label rootCauseTypeLabel = new Label(rootCauseGroup, SWT.NONE);
		rootCauseTypeLabel.setText("Root Cause Type");
		
		this.rootCauseLocatorTypeCombo = new Combo(rootCauseGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		final String[] selectionsNam = Stream.of(RootCauseLocatorType.values()).map(Enum::name).toArray(String[]::new);
		this.rootCauseLocatorTypeCombo.setItems(selectionsNam);
		this.rootCauseLocatorTypeCombo.select(this.rootCauseLocatorType.ordinal());
		
		GridData comboData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboData.horizontalAlignment = GridData.END;
		this.rootCauseLocatorTypeCombo.setLayoutData(comboData);
	}
	
	protected void createLogTextSetting(final Composite parent) {
		final Group rootCauseGroup = new Group(parent, SWT.NONE);
		rootCauseGroup.setText("Log File Settings");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		rootCauseGroup.setLayout(layout);
		
		rootCauseGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label logPathLabel = new Label(rootCauseGroup, SWT.NONE);
		logPathLabel.setText("Log Path");
		
//		this.logText = new Text(rootCauseGroup, SWT.BORDER);
//		this.logText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
//		this.logText.setText(this.logPath);
//		GridData javaHomeTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
//		this.logText.setLayoutData(javaHomeTextData);
	}
	
}
