package microbat.mutation.trace.preference;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import microbat.Activator;
import microbat.mutation.trace.MuRegressionRetriever;
import microbat.util.SWTFactory;
import microbat.util.WorkbenchUtils;

public class MuRegressionPreferenceOld extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String TARGET_PROJECT_KEY = "targetProject";
	public static final String BUG_ID_KEY = "bugId";
	
	/* components */
	private Combo projectCombo;
	private Combo bugIdCombo;
	
	/* other fields */
	private MuRegressionRetriever dbRetriver = new MuRegressionRetriever();
	private Map<String, List<String>> cacheBugIds = new HashMap<String, List<String>>();
	
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
		List<String> bugIds = cacheBugIds.get(targetProject);
		if (bugIds == null && targetProject != null) {
			try {
				bugIds = dbRetriver.getMuBugIds(targetProject);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			cacheBugIds.put(targetProject, bugIds);
		}
		String selectedBugId = bugIdCombo.getText();
		bugIdCombo.setItems((String[]) bugIds.toArray(new String[0]));
		bugIdCombo.setText(selectedBugId);
	}

	private void setDefaultValue() {
		projectCombo.setText(Activator.getDefault().getPreferenceStore().getString(TARGET_PROJECT_KEY));
		bugIdCombo.setText(Activator.getDefault().getPreferenceStore().getString(BUG_ID_KEY));
		updateBugIdList();
	}

	@Override
	public boolean performOk(){
		cacheBugIds.clear();
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode("muregression.preference");
		preferences.put(TARGET_PROJECT_KEY, this.projectCombo.getText());
		preferences.put(BUG_ID_KEY, this.bugIdCombo.getText());
		Activator.getDefault().getPreferenceStore().putValue(TARGET_PROJECT_KEY, this.projectCombo.getText());
		Activator.getDefault().getPreferenceStore().putValue(BUG_ID_KEY, this.bugIdCombo.getText());
		return true;
	}
}
