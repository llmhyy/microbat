package microbat.mutation.trace.preference;

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
import microbat.mutation.trace.dto.MutationCase;
import microbat.util.SWTFactory;
import microbat.util.WorkbenchUtils;
import sav.common.core.utils.ObjectUtils;

public class MuRegressionPreference extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String TARGET_PROJECT_KEY = "mutationTargetProject";
	public static final String BUG_ID_KEY = "bugId";
	public static final String RERUN_KEY = "rerun";
	
	/* components */
	private Combo projectCombo;
	private Combo bugIdCombo;
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
		
		SWTFactory.createLabel(compo, "", 2);
		SWTFactory.createLabel(compo, "");
		rerunCb = SWTFactory.createCheckbox(compo, "Execute to get trace Again");
		
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

	@Override
	public boolean performOk(){
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode("muregression.preference");
		preferences.put(TARGET_PROJECT_KEY, this.projectCombo.getText());
		String bugId = bugIdCombo.getText();
		preferences.put(BUG_ID_KEY, bugId);
		String isRerun = String.valueOf(rerunCb.getSelection());
		preferences.put(RERUN_KEY, isRerun);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		Activator.getDefault().getPreferenceStore().putValue(TARGET_PROJECT_KEY, this.projectCombo.getText());
		Activator.getDefault().getPreferenceStore().putValue(BUG_ID_KEY, bugId);
		Activator.getDefault().getPreferenceStore().putValue(RERUN_KEY, isRerun);
		return true;
	}
}
