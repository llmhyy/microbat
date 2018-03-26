package microbat.mutation.trace.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import microbat.Activator;
import microbat.mutation.mutation.MutationType;
import microbat.mutation.trace.dto.MutationCase;
import microbat.util.SWTFactory;
import microbat.util.WorkbenchUtils;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.ObjectUtils;
import sav.common.core.utils.StringUtils;

public class MutationRegressionPreference extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String TARGET_PROJECT_KEY = "mutationTargetProject";
	public static final String BUG_ID_KEY = "bugId";
	public static final String RERUN_KEY = "rerun";
	public static final String MUTATION_TYPES = "mutationTypes";
	
	/* components */
	private Combo projectCombo;
	private Combo bugIdCombo;
	private List<Button> mutationTypeCbs;
	private List<MutationType> mutationTypes = CollectionUtils.toArrayList(MutationType.values());
	private Button rerunCb;

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite compo = new Composite(parent, SWT.NONE);
		compo.setLayout(new GridLayout(2, false));
		
		SWTFactory.createLabel(compo, "Target Project");
		projectCombo = new Combo(compo, SWT.BORDER);
		SWTFactory.horizontalSpan(projectCombo, 1);
		projectCombo.setItems(WorkbenchUtils.getProjectsInWorkspace());
		
		SWTFactory.createLabel(compo, "Bug Id");
		bugIdCombo = new Combo(compo, SWT.BORDER);
		SWTFactory.horizontalSpan(bugIdCombo, 1);
		
		SWTFactory.createLabel(compo, "");
		Composite mutationTypeGroup = SWTFactory.createGroup(compo, "Mutation types", 2);
		mutationTypeCbs = new ArrayList<>(mutationTypes.size());
		for (MutationType type : mutationTypes) {
			mutationTypeCbs.add(SWTFactory.createCheckbox(mutationTypeGroup, type.getText()));
		}
		SWTFactory.createLabel(mutationTypeGroup, "");
		
		rerunCb = SWTFactory.createCheckbox(compo, "Execute to get trace Again", 2);
		setDefaultValue();
		registerListener();
		return compo;
	}

	private void registerListener() {
		projectCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateBugIdList();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateBugIdList();
			}
			
		});
	}
	
	protected void updateBugIdList() {
		String targetProject = projectCombo.getText();
		List<String> bugIds = MutationCase.loadAllMutationBugIds(targetProject);
		
		String selectedBugId = bugIdCombo.getText();
		bugIdCombo.setItems((String[]) bugIds.toArray(new String[0]));
		if (bugIds.contains(selectedBugId)) {
			bugIdCombo.setText(selectedBugId);
		} else {
			bugIdCombo.setText("");
		}
	}

	private void setDefaultValue() {
		projectCombo.setText(getTargetProject());
		bugIdCombo.setText(getMuBugId());
		rerunCb.setSelection(getRerunFlag());
		List<MutationType> selectedMutationTypes = getSelectedMutationTypes();
		for (int i = 0; i < mutationTypes.size(); i++) {
			boolean selection = selectedMutationTypes.contains(mutationTypes.get(i));
			mutationTypeCbs.get(i).setSelection(selection);
		}
		updateBugIdList();
		
	}

	public static String getMuBugId() {
		return Activator.getDefault().getPreferenceStore().getString(BUG_ID_KEY);
	}
	
	public static boolean getRerunFlag() {
		String strVal = Activator.getDefault().getPreferenceStore().getString(RERUN_KEY);
		return ObjectUtils.toBoolean(strVal, false);
	}

	public static String getTargetProject() {
		return Activator.getDefault().getPreferenceStore().getString(TARGET_PROJECT_KEY);
	}
	
	public static List<MutationType> getSelectedMutationTypes() {
		String strVal = Activator.getDefault().getPreferenceStore().getString(MUTATION_TYPES);
		if (StringUtils.isEmpty(strVal)) {
			return Collections.emptyList();
		}
		String[] types = strVal.split(";");
		List<MutationType> mutationTypes = new ArrayList<>(types.length);
		for (String type : types) {
			mutationTypes.add(MutationType.valueOf(type));
		}
		return mutationTypes;
	}
	
	public String collectSelectedMutationTypes() {
		List<MutationType> selectedMutationTypes = new ArrayList<>();
		for (int i = 0; i < mutationTypeCbs.size(); i++) {
			if (mutationTypeCbs.get(i).getSelection()) {
				selectedMutationTypes.add(mutationTypes.get(i));
			}
		}
		return StringUtils.join(selectedMutationTypes, ";");
	}

	@Override
	public boolean performOk(){
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode("muregression.preference");
		preferences.put(TARGET_PROJECT_KEY, this.projectCombo.getText());
		String bugId = bugIdCombo.getText();
		preferences.put(BUG_ID_KEY, bugId);
		String isRerun = String.valueOf(rerunCb.getSelection());
		preferences.put(RERUN_KEY, isRerun);
		String selectedMutationTypes = collectSelectedMutationTypes();
		preferences.put(MUTATION_TYPES, selectedMutationTypes);
		
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		Activator.getDefault().getPreferenceStore().putValue(TARGET_PROJECT_KEY, this.projectCombo.getText());
		Activator.getDefault().getPreferenceStore().putValue(BUG_ID_KEY, bugId);
		Activator.getDefault().getPreferenceStore().putValue(RERUN_KEY, isRerun);
		Activator.getDefault().getPreferenceStore().putValue(MUTATION_TYPES, selectedMutationTypes);
		return true;
	}
}
