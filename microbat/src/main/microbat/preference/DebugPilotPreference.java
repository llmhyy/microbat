package microbat.preference;

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
	public static final String USE_LOCAL_SERVER_KEY = "DP/USE_LOCAL_SERVER";
	public static final String SERVER_HOST_KEY = "DP/SERVER_HOST";
	public static final String SERVER_PORT_KEY = "DP/SERVER_PORT";
	
	/* Probability Propagation Settings */
	protected Combo propagatorTypeCombo;
	protected PropagatorType propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
	
	/* Path Finder Settings */
	protected Combo pathFinderTypeCombo;
	protected PathFinderType pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
	
	/* Root Cause Locator Settings */
	protected Combo rootCauseLocatorTypeCombo;
	protected RootCauseLocatorType rootCauseLocatorType = RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE;
	
	/* Local Server Settings*/
	protected Button useLocalServerButton;
	protected Text serverHostTextBox;
	protected Text serverPortTextBox;
	protected boolean useLocalServer = PropagatorSettings.DEFAULT_USE_LOCATL_SERVER;
	protected String serverHost = PropagatorSettings.DEFAULT_SERVER_HOST;
	protected int serverPort = PropagatorSettings.DEFAULT_SERVER_PORT;
	
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
			this.useLocalServer = Boolean.valueOf(Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.USE_LOCAL_SERVER_KEY));
			this.serverHost = Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.SERVER_HOST_KEY);
			this.serverPort = Integer.valueOf(Activator.getDefault().getPreferenceStore().getString(DebugPilotPreference.SERVER_PORT_KEY));
		} catch (IllegalArgumentException e) {
			this.propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
			this.pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
			this.rootCauseLocatorType = RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE;
			this.useLocalServer = PropagatorSettings.DEFAULT_USE_LOCATL_SERVER;
			this.serverHost = PropagatorSettings.DEFAULT_SERVER_HOST;
			this.serverPort = PropagatorSettings.DEFAULT_SERVER_PORT;
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout());
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.createPropagatorSetting(contents);
		this.createPathFinderSetting(contents);
		this.createRootCauseLocatorSetting(contents);
//		this.createLocalServerSetting(contents);
		return contents;
	}
	
	@Override
	public boolean performOk() {
		this.propagatorType = PropagatorType.valueOf(this.propagatorTypeCombo.getText());
		this.pathFinderType = PathFinderType.valueOf(this.pathFinderTypeCombo.getText());
		this.rootCauseLocatorType = RootCauseLocatorType.valueOf(this.rootCauseLocatorTypeCombo.getText());
		this.useLocalServer = this.useLocalServerButton.getSelection();
		this.serverHost = this.serverHostTextBox.getText();
		this.serverPort = Integer.valueOf(this.serverPortTextBox.getText());
		this.storeData();
		return true;
	}
	
	@Override
	public void performDefaults() {
		super.performDefaults();
		this.propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
		this.pathFinderType = PathFinderSettings.DEFAULT_PATH_FINDER_TYPE;
		this.rootCauseLocatorType = RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE;
		this.useLocalServer = PropagatorSettings.DEFAULT_USE_LOCATL_SERVER;
		this.serverHost = PropagatorSettings.DEFAULT_SERVER_HOST;
		this.serverPort = PropagatorSettings.DEFAULT_SERVER_PORT;
		
		this.propagatorTypeCombo.select(this.propagatorType.ordinal());
		this.pathFinderTypeCombo.select(this.pathFinderType.ordinal());
		this.rootCauseLocatorTypeCombo.select(this.rootCauseLocatorType.ordinal());
		this.useLocalServerButton.setSelection(this.useLocalServer);
		this.serverHostTextBox.setText(this.serverHost);
		this.serverPortTextBox.setText(String.valueOf(this.serverPort));
		this.storeData();
	}
	
	protected void storeData() {
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(DebugPilotPreference.ID);
		preferences.put(DebugPilotPreference.PROPAGATOR_TYPE_KEY, this.propagatorType.name());
		preferences.put(DebugPilotPreference.PATHFINDER_TYPE_KEY, this.pathFinderType.name());
		preferences.put(DebugPilotPreference.ROOT_CAUSE_LOCATOR_TYPE_KEY, this.rootCauseLocatorType.name());
		preferences.put(DebugPilotPreference.USE_LOCAL_SERVER_KEY, String.valueOf(this.useLocalServerButton.getSelection()));
		preferences.put(DebugPilotPreference.SERVER_HOST_KEY, this.serverHost);
		preferences.put(DebugPilotPreference.SERVER_PORT_KEY, String.valueOf(this.serverPort));
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.PROPAGATOR_TYPE_KEY, this.propagatorType.name());
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.PATHFINDER_TYPE_KEY, this.pathFinderType.name());
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.ROOT_CAUSE_LOCATOR_TYPE_KEY, this.rootCauseLocatorType.name());
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.USE_LOCAL_SERVER_KEY, String.valueOf(this.useLocalServerButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.SERVER_HOST_KEY, this.serverHost);
		Activator.getDefault().getPreferenceStore().putValue(DebugPilotPreference.SERVER_PORT_KEY, String.valueOf(this.serverPort));
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
	
	protected void createLocalServerSetting(final Composite parent) {
		final Group localServerGroup = new Group(parent, SWT.NONE);
		localServerGroup.setText("Server Settings");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		localServerGroup.setLayout(layout);
		
		localServerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		/* Use local server or not*/
		this.useLocalServerButton = new Button(localServerGroup, SWT.CHECK);
		this.useLocalServerButton.setText("Use local server");
		this.useLocalServerButton.setSelection(this.useLocalServer);
		this.useLocalServerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				triggerLocalServerSetting();
			}
		});
		
		// Empty label for formatting
		@SuppressWarnings("unused")
		Label emtpyLable = new Label(localServerGroup, SWT.NONE);
		
		/* Server host setting */
		Label serverHostLabel = new Label(localServerGroup, SWT.NONE);
		serverHostLabel.setText("Server Host: ");
		this.serverHostTextBox = new Text(localServerGroup, SWT.BORDER);
		this.serverHostTextBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.serverHostTextBox.setMessage("Server Host");
		this.serverHostTextBox.setText(this.serverHost);
		this.serverHostTextBox.setEnabled(this.useLocalServer);
		
		/* Server port setting */
		Label serverPortLabel = new Label(localServerGroup, SWT.NONE);
		serverPortLabel.setText("Server Port: ");
		this.serverPortTextBox = new Text(localServerGroup, SWT.BORDER);
		this.serverPortTextBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.serverPortTextBox.setMessage("Server Port");
		this.serverPortTextBox.setText(String.valueOf(this.serverPort));
		this.serverPortTextBox.setEnabled(this.useLocalServer);
	}
	
	protected void triggerLocalServerSetting() {
		boolean isEnabled = this.useLocalServerButton.getSelection();
		this.serverHostTextBox.setEnabled(isEnabled);
		this.serverPortTextBox.setEnabled(isEnabled);
		
		final String serverHostText = isEnabled ? this.serverHost : PropagatorSettings.DEFAULT_SERVER_HOST;
		final String serverPortText = String.valueOf(isEnabled ? this.serverPort : PropagatorSettings.DEFAULT_SERVER_PORT);
		this.serverHostTextBox.setText(serverHostText);
		this.serverPortTextBox.setText(serverPortText);
	}
}
