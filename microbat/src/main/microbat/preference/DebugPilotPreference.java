package microbat.preference;

import java.util.stream.Stream;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import microbat.Activator;
import microbat.debugpilot.pathfinding.PathFinderType;
import microbat.debugpilot.propagation.PropagatorType;

public class DebugPilotPreference extends PreferencePage implements IWorkbenchPreferencePage  {

	private static final String ID = "microbat.preference.debugpilot";
	
	protected static final PropagatorType DEFAULT_PROPAGATOR_TYPE = PropagatorType.SPP_COST;
	protected static final PathFinderType DEFEAUL_PATH_FINDER_TYPE = PathFinderType.Dijstra;
	
	public static final String PROPAGATOR_TYPE = "PROPAGATOR_TYPE";
	public static final String PATHFINDER_TYPE = "PATHFINDER_TYPE";
	
	protected PropagatorType propagatorType = DebugPilotPreference.DEFAULT_PROPAGATOR_TYPE;
	protected PathFinderType pathFinderType = DebugPilotPreference.DEFEAUL_PATH_FINDER_TYPE;
	
	protected Combo propagatorTypeCombo;
	protected Combo pathFinderTypeCombo;
	
	public DebugPilotPreference() {
		super("Debug Pilot Settings");
		this.setDescription("Configuration for debug pilot");
	}
	
	@Override
	public void init(IWorkbench workbench) {
		try {			
			this.propagatorType = PropagatorType.valueOf(Activator.getDefault().getPreferenceStore().getDefaultString(DebugPilotPreference.PROPAGATOR_TYPE));
			this.pathFinderType = PathFinderType.valueOf(Activator.getDefault().getPreferenceStore().getDefaultString(DebugPilotPreference.PATHFINDER_TYPE));
		} catch (IllegalArgumentException e) {
			this.propagatorType = DebugPilotPreference.DEFAULT_PROPAGATOR_TYPE;
			this.pathFinderType = DebugPilotPreference.DEFEAUL_PATH_FINDER_TYPE;
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout());
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.createPropagatorSetting(contents);
		this.createPathFinderSetting(contents);
		return contents;
	}
	
	@Override
	public boolean performOk() {
		this.propagatorType = PropagatorType.valueOf(this.propagatorTypeCombo.getText());
		this.pathFinderType = PathFinderType.valueOf(this.pathFinderTypeCombo.getText());
		this.storeData();
		return true;
	}
	
	@Override
	public void performDefaults() {
		super.performDefaults();
		this.propagatorType = DebugPilotPreference.DEFAULT_PROPAGATOR_TYPE;
		this.pathFinderType = DebugPilotPreference.DEFEAUL_PATH_FINDER_TYPE;
		this.propagatorTypeCombo.select(this.propagatorType.ordinal());
		this.pathFinderTypeCombo.select(this.pathFinderType.ordinal());
		this.storeData();
	}
	
	protected void storeData() {
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(DebugPilotPreference.ID);
		preferences.put(DebugPilotPreference.PROPAGATOR_TYPE, this.propagatorType.name());
		preferences.put(DebugPilotPreference.PATHFINDER_TYPE, this.pathFinderType.name());
		
		Activator.getDefault().getPreferenceStore().setDefault(DebugPilotPreference.PROPAGATOR_TYPE, this.propagatorType.name());
		Activator.getDefault().getPreferenceStore().setValue(DebugPilotPreference.PROPAGATOR_TYPE, this.propagatorType.name());
		Activator.getDefault().getPreferenceStore().setDefault(DebugPilotPreference.PATHFINDER_TYPE, this.pathFinderType.name());
		Activator.getDefault().getPreferenceStore().setValue(DebugPilotPreference.PATHFINDER_TYPE, this.pathFinderType.name());		
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

}
