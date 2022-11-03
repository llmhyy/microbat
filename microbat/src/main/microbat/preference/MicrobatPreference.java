package microbat.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import microbat.Activator;
import microbat.util.SWTFactory;
import microbat.util.Settings;

public class MicrobatPreference extends PreferencePage implements
		IWorkbenchPreferencePage {

	public MicrobatPreference() {
	}

	public MicrobatPreference(String title) {
		super(title);
	}

	public MicrobatPreference(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		this.defaultTargetProject = Activator.getDefault().getPreferenceStore().getString(TARGET_PORJECT);
		this.defaultClassName = Activator.getDefault().getPreferenceStore().getString(CLASS_NAME);
		this.defaultTestMethod = Activator.getDefault().getPreferenceStore().getString(TEST_METHOD);
		this.defaultLineNumber = Activator.getDefault().getPreferenceStore().getString(LINE_NUMBER);
		this.defaultLanuchClass = Activator.getDefault().getPreferenceStore().getString(LANUCH_CLASS);
		this.defaultSupportConcurrentTrace = Activator.getDefault().getPreferenceStore().getString(SUPPORT_CONCURRENT_TRACE);
		this.defaultRecordSnapshot = Activator.getDefault().getPreferenceStore().getString(RECORD_SNAPSHORT);
		this.defaultRunWithDebugMode = Activator.getDefault().getPreferenceStore().getString(RUN_WITH_DEBUG_MODE);
		this.defaultAdvancedDetailInspector = Activator.getDefault().getPreferenceStore().getString(APPLY_ADVANCE_INSPECTOR);
		this.defaultStepLimit = getStepLimit();
		this.defaultRunTest = Activator.getDefault().getPreferenceStore().getString(RUN_TEST);
		this.defaultVariableLayer = getVariableValue();
		this.defaultJava7HomePath = Activator.getDefault().getPreferenceStore().getString(JAVA7HOME_PATH);
		this.defaultApplyRecodingOptimization = Activator.getDefault().getPreferenceStore().getString(RECORDING_OPTIMIZATION);
		this.defaultEnableMethodSplitting = Activator.getDefault().getPreferenceStore().getBoolean(REQUIRE_METHOD_SPLITTING);
		this.defaultRunWithDebugMode = Activator.getDefault().getPreferenceStore().getString(RUN_WITH_DEBUG_MODE);
//		this.defaultProjectPath = Activator.getDefault().getPreferenceStore().getString(PROJECT_PATH);
//		this.defaultDropInFolder = Activator.getDefault().getPreferenceStore().getString(DROP_IN_FOLDER_MICROBAT);
//		this.defaultConfigPath = Activator.getDefault().getPreferenceStore().getString(CONFIG_PATH_MICROBAT);
		this.defaultTestCaseID = Activator.getDefault().getPreferenceStore().getString(TEST_CASE_ID_MICROBAT);
		this.defaultUseTestCaseID = Activator.getDefault().getPreferenceStore().getString(USE_TEST_CASE_ID);
		
	}

	public static String getStepLimit() {
		return Activator.getDefault().getPreferenceStore().getString(STEP_LIMIT);
	}

	public static String getVariableValue() {
		return Activator.getDefault().getPreferenceStore().getString(VARIABLE_LAYER);
	}
	
	public static String getValue(String key) {
		return Activator.getDefault().getPreferenceStore().getString(key);
	}
	
	public static final String TARGET_PORJECT = "targetProjectName";
	public static final String CLASS_NAME = "className";
	public static final String LINE_NUMBER = "lineNumber";
	public static final String LANUCH_CLASS = "lanuchClass";
	public static final String STEP_LIMIT = "stepLimit";
	public static final String RECORD_SNAPSHORT = "recordSnapshot";
	public static final String APPLY_ADVANCE_INSPECTOR = "applyAdvancedInspector";
	public static final String TEST_METHOD = "testMethod";
	public static final String RUN_TEST = "isRunTest";
	public static final String VARIABLE_LAYER = "variableLayer";
	public static final String JAVA7HOME_PATH = "java7_path";
	public static final String RECORDING_OPTIMIZATION = "recording_optimization";
	public static final String REQUIRE_METHOD_SPLITTING = "enableMethodSplitting";
	public static final String SUPPORT_CONCURRENT_TRACE = "supportConcurrentTrace";
	public static final String RUN_WITH_DEBUG_MODE = "runWithDebugMode";
//	public static final String DROP_IN_FOLDER_MICROBAT = "dropInFolder";
//	public static final String CONFIG_PATH_MICROBAT = "configPath";
//	public static final String PROJECT_PATH = "projectPath";
	public static final String TEST_CASE_ID_MICROBAT = "testCaseID";
	public static final String USE_TEST_CASE_ID = "useTestCaseID";

	private Combo projectCombo;
	private Text lanuchClassText;
	private Text testMethodText;
	private Text classNameText;
	private Text lineNumberText;
	private Text stepLimitText;
	private Text variableLayerText;
	private Button recordSnapshotButton;
	private Button supportConcurrentTraceButton;
	private Button recordingOptimizationButton;
	private Button advancedDetailInspectorButton;
	private Button runTestButton;
	private Button runWithDebugModeButton;
	private Button enableMethodSplittingButton;
	private Text java7HomePathText;
	private Button useTestCaseIDButton;
//	private Text dropInFolderText;
//	private Text configPathText;
//	private Text projectPathText;
	private Text testCaseIDText;
	
//	private Combo autoFeedbackCombo;
	
	private String defaultTargetProject = "";
	private String defaultLanuchClass = "";
	private String defaultTestMethod = "";
	private String defaultClassName = "";
	private String defaultLineNumber = "";
	private String defaultVariableLayer = "";
	private String defaultStepLimit = "5000";
	private String defaultRecordSnapshot = "true";
	private String defaultSupportConcurrentTrace = "false";
	private String defaultAdvancedDetailInspector = "true";
	private String defaultRunTest = "false";
	private String defaultJava7HomePath;
	private String defaultApplyRecodingOptimization;
	private String defaultRunWithDebugMode = "false";
	private boolean defaultEnableMethodSplitting;
//	private String defaultDropInFolder;
//	private String defaultConfigPath;
//	private String defaultProjectPath;
	private String defaultTestCaseID;
	private String defaultUseTestCaseID;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		
		composite.setLayout(layout);
		
		Label projectLabel = new Label(composite, SWT.NONE);
		projectLabel.setText("Target Project");
		
		projectCombo = new Combo(composite, SWT.BORDER);
		projectCombo.setItems(getProjectsInWorkspace());
		projectCombo.setText(this.defaultTargetProject);
		GridData comboData = new GridData(SWT.FILL, SWT.FILL, true, false);
		comboData.horizontalSpan = 2;
		projectCombo.setLayoutData(comboData);
		
		createSettingGroup(composite);
		createSeedStatementGroup(composite);

		return composite;
	}
	
	private void createSettingGroup(Composite parent){
		Group settingGroup = new Group(parent, SWT.NONE);
		settingGroup.setText("Settings");
		GridData seedStatementGroupData = new GridData(SWT.FILL, SWT.FILL, true, true);
		seedStatementGroupData.horizontalSpan = 3;
		settingGroup.setLayoutData(seedStatementGroupData);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		
		settingGroup.setLayout(layout);
		
		Label java7HomeLabel = new Label(settingGroup, SWT.NONE);
		java7HomeLabel.setText("Java Home Path: ");
		java7HomePathText = new Text(settingGroup, SWT.BORDER);
		java7HomePathText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		java7HomePathText.setText(this.defaultJava7HomePath);
		GridData javaHomeTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
		javaHomeTextData.horizontalSpan = 2;
		java7HomePathText.setLayoutData(javaHomeTextData);
		
		Label stepLimitLabel = new Label(settingGroup, SWT.NONE);
		stepLimitLabel.setText("Step Limit: ");
		stepLimitText = new Text(settingGroup, SWT.BORDER);
		stepLimitText.setText(this.defaultStepLimit);
		GridData stepLimitTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
		stepLimitTextData.horizontalSpan = 2;
		stepLimitText.setLayoutData(stepLimitTextData);
		
		Label variableLayerLabel = new Label(settingGroup, SWT.NONE);
		variableLayerLabel.setText("Variable Layer: ");
		variableLayerText = new Text(settingGroup, SWT.BORDER);
		variableLayerText.setText(this.defaultVariableLayer);
		GridData variableLayerTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
		variableLayerTextData.horizontalSpan = 2;
		variableLayerText.setLayoutData(variableLayerTextData);
		variableLayerText.setToolTipText("how many layers of variable children does the debugger need to retrieve, -1 means infinite.");
		
		supportConcurrentTraceButton = new Button(settingGroup, SWT.CHECK);
		supportConcurrentTraceButton.setText("Support concurrent trace");
		GridData supportConcurrentTraceButtonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		supportConcurrentTraceButtonData.horizontalSpan = 3;
		supportConcurrentTraceButton.setLayoutData(supportConcurrentTraceButtonData);
		boolean supportConcurrentTraceSelected = this.defaultSupportConcurrentTrace.equals("true");
		supportConcurrentTraceButton.setSelection(supportConcurrentTraceSelected);
		
		recordSnapshotButton = new Button(settingGroup, SWT.CHECK);
		recordSnapshotButton.setText("Record snapshot");
		GridData recordButtonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		recordButtonData.horizontalSpan = 3;
		recordSnapshotButton.setLayoutData(recordButtonData);
		boolean recordSnapshotSelected = this.defaultRecordSnapshot.equals("true");
		recordSnapshotButton.setSelection(recordSnapshotSelected);
		
		runWithDebugModeButton = new Button(settingGroup, SWT.CHECK);
		runWithDebugModeButton.setText("Run with debug mode");
		GridData runWithDebugModeButtonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		runWithDebugModeButtonData.horizontalSpan = 3;
		runWithDebugModeButton.setLayoutData(runWithDebugModeButtonData);
		boolean runWithDebugModeButtonSelected = this.defaultRunWithDebugMode.equals("true");
		runWithDebugModeButton.setSelection(runWithDebugModeButtonSelected);
		
		advancedDetailInspectorButton = new Button(settingGroup, SWT.CHECK);
		advancedDetailInspectorButton.setText("Apply advanced detail inspection");
		GridData advanceInspectorButtonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		recordButtonData.horizontalSpan = 3;
		advancedDetailInspectorButton.setLayoutData(advanceInspectorButtonData);
		boolean advanceInspectorSelected = this.defaultAdvancedDetailInspector.equals("true");
		advancedDetailInspectorButton.setSelection(advanceInspectorSelected);
		
		recordingOptimizationButton = new Button(settingGroup, SWT.CHECK);
		recordingOptimizationButton.setText("Apply trace recording optimization for interested library code");
		GridData recordingOptimizationButtonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		recordingOptimizationButtonData.horizontalSpan = 3;
		recordingOptimizationButton.setLayoutData(recordingOptimizationButtonData);
		boolean recordingOptimizationSelected = this.defaultApplyRecodingOptimization.equals("true");
		recordingOptimizationButton.setSelection(recordingOptimizationSelected);
		
		enableMethodSplittingButton = SWTFactory.createCheckbox(settingGroup, "Enable method splitting function", 2);
		enableMethodSplittingButton.setSelection(this.defaultEnableMethodSplitting);
	}
	
	private void createSeedStatementGroup(Composite parent){
		Group seedStatementGroup = new Group(parent, SWT.NONE);
		seedStatementGroup.setText("Seed statement");
		GridData seedStatementGroupData = new GridData(SWT.FILL, SWT.FILL, true, true);
		seedStatementGroupData.horizontalSpan = 3;
		seedStatementGroup.setLayoutData(seedStatementGroupData);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		
		seedStatementGroup.setLayout(layout);
		
		runTestButton = new Button(seedStatementGroup, SWT.CHECK);
		runTestButton.setText("is to run JUnit test?");
		GridData runTestButtonDataButtonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		runTestButtonDataButtonData.horizontalSpan = 3;
		runTestButton.setLayoutData(runTestButtonDataButtonData);
		boolean runTestSelected = this.defaultRunTest.equals("true");
		runTestButton.setSelection(runTestSelected);
		
		Label lanuchClassLabel = new Label(seedStatementGroup, SWT.NONE);
		lanuchClassLabel.setText("Lanuch Class: ");
		lanuchClassText = new Text(seedStatementGroup, SWT.BORDER);
		lanuchClassText.setText(this.defaultLanuchClass);
		GridData lanuchClassTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
		lanuchClassTextData.horizontalSpan = 2;
		lanuchClassText.setLayoutData(lanuchClassTextData);
		
		Label testMethodLabel = new Label(seedStatementGroup, SWT.NONE);
		testMethodLabel.setText("Test Method: ");
		testMethodText = new Text(seedStatementGroup, SWT.BORDER);
		testMethodText.setText(this.defaultTestMethod);
		GridData testMethoDataData = new GridData(SWT.FILL, SWT.FILL, true, false);
		testMethoDataData.horizontalSpan = 2;
		testMethodText.setLayoutData(testMethoDataData);
		
		// Create setting UI for mutation
		this.createMutationSeetingGroup(parent);
		
//		Label classNameLabel = new Label(seedStatementGroup, SWT.NONE);
//		classNameLabel.setText("Class Name: ");
//		classNameText = new Text(seedStatementGroup, SWT.BORDER);
//		classNameText.setText(this.defaultClassName);
//		GridData classNameTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
//		classNameTextData.horizontalSpan = 2;
//		classNameText.setLayoutData(classNameTextData);
//		
//		Label lineNumberLabel = new Label(seedStatementGroup, SWT.NONE);
//		lineNumberLabel.setText("Line Number: ");
//		lineNumberText = new Text(seedStatementGroup, SWT.BORDER);
//		lineNumberText.setText(this.defaultLineNumber);
//		GridData lineNumTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
//		lineNumTextData.horizontalSpan = 2;
//		lineNumberText.setLayoutData(lineNumTextData);
		
	}
	
	public boolean performOk(){
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode("microbat.preference");
		preferences.put(TARGET_PORJECT, this.projectCombo.getText());
		preferences.put(LANUCH_CLASS, this.lanuchClassText.getText());
		preferences.put(TEST_METHOD, this.testMethodText.getText());
//		preferences.put(CLASS_NAME, this.classNameText.getText());
//		preferences.put(LINE_NUMBER, this.lineNumberText.getText());
		preferences.put(RECORD_SNAPSHORT, String.valueOf(this.recordSnapshotButton.getSelection()));
		preferences.put(APPLY_ADVANCE_INSPECTOR, String.valueOf(this.advancedDetailInspectorButton.getSelection()));
		preferences.put(STEP_LIMIT, this.stepLimitText.getText());
		preferences.put(VARIABLE_LAYER, this.variableLayerText.getText());
		preferences.put(RUN_TEST, String.valueOf(this.runTestButton.getSelection()));
		preferences.put(JAVA7HOME_PATH, this.java7HomePathText.getText());
		preferences.put(RECORDING_OPTIMIZATION, String.valueOf(this.recordingOptimizationButton.getSelection()));
		preferences.putBoolean(REQUIRE_METHOD_SPLITTING, this.enableMethodSplittingButton.getSelection());
		preferences.put(SUPPORT_CONCURRENT_TRACE, String.valueOf(this.supportConcurrentTraceButton.getSelection()));
		preferences.put(RUN_WITH_DEBUG_MODE, String.valueOf(this.runWithDebugModeButton.getSelection()));
//		preferences.put(DROP_IN_FOLDER_MICROBAT, this.dropInFolderText.getText());
//		preferences.put(CONFIG_PATH_MICROBAT, this.configPathText.getText());
//		preferences.put(PROJECT_PATH, this.projectPathText.getText());
		preferences.put(TEST_CASE_ID_MICROBAT, this.testCaseIDText.getText());
		preferences.put(USE_TEST_CASE_ID, String.valueOf(this.useTestCaseIDButton.getSelection()));
		
		Activator.getDefault().getPreferenceStore().putValue(TARGET_PORJECT, this.projectCombo.getText());
		Activator.getDefault().getPreferenceStore().putValue(LANUCH_CLASS, this.lanuchClassText.getText());
		Activator.getDefault().getPreferenceStore().putValue(TEST_METHOD, this.testMethodText.getText());
//		Activator.getDefault().getPreferenceStore().putValue(CLASS_NAME, this.classNameText.getText());
//		Activator.getDefault().getPreferenceStore().putValue(LINE_NUMBER, this.lineNumberText.getText());
		Activator.getDefault().getPreferenceStore().putValue(RECORD_SNAPSHORT, String.valueOf(this.recordSnapshotButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(APPLY_ADVANCE_INSPECTOR, String.valueOf(this.advancedDetailInspectorButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(STEP_LIMIT, this.stepLimitText.getText());
		Activator.getDefault().getPreferenceStore().putValue(VARIABLE_LAYER, this.variableLayerText.getText());
		Activator.getDefault().getPreferenceStore().putValue(RUN_TEST, String.valueOf(this.runTestButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(JAVA7HOME_PATH, this.java7HomePathText.getText());
		Activator.getDefault().getPreferenceStore().putValue(RECORDING_OPTIMIZATION, String.valueOf(this.recordingOptimizationButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(REQUIRE_METHOD_SPLITTING, String.valueOf(this.enableMethodSplittingButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(SUPPORT_CONCURRENT_TRACE, String.valueOf(this.supportConcurrentTraceButton.getSelection()));
		Activator.getDefault().getPreferenceStore().putValue(RUN_WITH_DEBUG_MODE, String.valueOf(this.runWithDebugModeButton.getSelection()));
//		Activator.getDefault().getPreferenceStore().putValue(DROP_IN_FOLDER_MICROBAT, this.dropInFolderText.getText());
//		Activator.getDefault().getPreferenceStore().putValue(CONFIG_PATH_MICROBAT, this.configPathText.getText());
//		Activator.getDefault().getPreferenceStore().putValue(PROJECT_PATH, this.projectPathText.getText());
		Activator.getDefault().getPreferenceStore().putValue(TEST_CASE_ID_MICROBAT, this.testCaseIDText.getText());
		Activator.getDefault().getPreferenceStore().putValue(USE_TEST_CASE_ID, String.valueOf(this.useTestCaseIDButton.getSelection()));
		
		confirmChanges();
		
		return true;
		
	}
	
	private void confirmChanges(){
		Settings.projectName = this.projectCombo.getText();
		Settings.launchClass = this.lanuchClassText.getText();
		Settings.testMethod = this.testMethodText.getText();
//		Settings.buggyClassName = this.classNameText.getText();
//		Settings.buggyLineNumber = this.lineNumberText.getText();
		Settings.isRecordSnapshot = this.recordSnapshotButton.getSelection();
		Settings.isApplyAdvancedInspector = this.advancedDetailInspectorButton.getSelection();
		Settings.stepLimit = Integer.valueOf(this.stepLimitText.getText());
		Settings.setVariableLayer(Integer.valueOf(this.variableLayerText.getText()));
		Settings.isRunTest = this.runTestButton.getSelection();
		Settings.applyLibraryOptimization = this.recordingOptimizationButton.getSelection();
		Settings.supportConcurrentTrace = this.supportConcurrentTraceButton.getSelection();
		Settings.isRunWtihDebugMode = this.runWithDebugModeButton.getSelection();
//		Settings.autoFeedbackMethod = this.autoFeedbackCombo.getText();
		
	}
	
	private String[] getProjectsInWorkspace(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		
		String[] projectStrings = new String[projects.length];
		for(int i=0; i<projects.length; i++){
			projectStrings[i] = projects[i].getName();
		}
		
		return projectStrings;
	}
	
	private void createMutationSeetingGroup(Composite parent) {
		Group mutationGroup = new Group(parent, SWT.NONE);
		mutationGroup.setText("Select Test Case By ID");
		
		GridData mutationGroupData = new GridData(SWT.FILL, SWT.FILL, true, true);
		mutationGroupData.horizontalSpan = 3;
		mutationGroup.setLayoutData(mutationGroupData);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		
		mutationGroup.setLayout(layout);
		
		this.useTestCaseIDButton = new Button(mutationGroup, SWT.CHECK);
		this.useTestCaseIDButton.setText("Use Test Case ID");
		GridData useTestCaseIDData = new GridData(SWT.FILL, SWT.FILL, true, false);
		useTestCaseIDData.horizontalSpan = 3;
		this.useTestCaseIDButton.setLayoutData(useTestCaseIDData);
		boolean useTestCaseIDSelected = this.defaultUseTestCaseID.equals("true");
		this.useTestCaseIDButton.setSelection(useTestCaseIDSelected);
		
//		Label projectPathLabel = new Label(mutationGroup, SWT.NONE);
//		projectPathLabel.setText("Project Path: ");
//		this.projectPathText = new Text(mutationGroup, SWT.NONE);
//		this.projectPathText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
//		this.projectPathText.setText(this.defaultProjectPath);
//		
//		Label dropInFolderLabel = new Label(mutationGroup, SWT.NONE);
//		dropInFolderLabel.setText("Drop In Folder: ");
//		this.dropInFolderText = new Text(mutationGroup, SWT.NONE);
//		this.dropInFolderText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
//		this.dropInFolderText.setText(this.defaultDropInFolder);
//		
//		Label configPathLabel = new Label(mutationGroup, SWT.NONE);
//		configPathLabel.setText("Config Path: ");
//		this.configPathText = new Text(mutationGroup, SWT.NONE);
//		this.configPathText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
//		this.configPathText.setText(this.defaultConfigPath);
		
		Label testCaseIDLable = new Label(mutationGroup, SWT.NONE);
		testCaseIDLable.setText("Test ID: ");
		this.testCaseIDText = new Text(mutationGroup, SWT.NONE);
		this.testCaseIDText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		this.testCaseIDText.setText(this.defaultTestCaseID);
		
	}
}
