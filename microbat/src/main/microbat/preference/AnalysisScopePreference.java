package microbat.preference;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import microbat.Activator;
import microbat.codeanalysis.runtime.Executor;
import microbat.ui.component.SWTFactory;
import microbat.util.MessageDialogs;
import sav.common.core.utils.StringUtils;

public class AnalysisScopePreference extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String EXCLUDED_LIBS = "excludedLibs";
	public static final String INCLUDED_LIBS = "includedLibs";
	private static final String ID = "microbat.preference.analysisScope";
	private static final String LIBS_SEPARATOR = ";";
	private AnalysisScopesTablePanel excludedTable;
	private AnalysisScopesTablePanel includedTable;
	
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label hintLbl = SWTFactory.createLabel(content, "Specify the libraries should be excluded/included when buiding trace. ");
		hintLbl.setFont(JFaceResources.getFontRegistry().getItalic(hintLbl.getFont().toString()));
		SWTFactory.createLabel(content, "Excluded Libraries:");
		excludedTable = new AnalysisScopesTablePanel(content);
		SWTFactory.createLabel(content, "Included Libraries:");
		includedTable = new AnalysisScopesTablePanel(content);
		setDefaultValues();
		return content;
	}

	private String transformArrayToString(String[] array) {
		StringBuffer buffer = new StringBuffer();
		for(String str: array) {
			buffer.append(str+";");
		}
		String lib = buffer.toString();
		lib = lib.substring(0, lib.length()-1);
		return lib;
	}
	
	private String[] transformStringToArray(String string) {
		String[] array = string.split(";");
		return array;
	}
	
	private void setDefaultValues() {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		String excludedStr = pref.getString(EXCLUDED_LIBS);
		if (!StringUtils.isEmpty(excludedStr)) {
			String[] filters = excludedStr.split(LIBS_SEPARATOR);
			excludedTable.setValue(filters);
		}
		else {
			excludedTable.setValue(Executor.libExcludes);
		}
		
		String includedStr = pref.getString(INCLUDED_LIBS);
		if (!StringUtils.isEmpty(includedStr)) {
			String[] filters = includedStr.split(LIBS_SEPARATOR);
			includedTable.setValue(filters);
		}
		else {
			includedTable.setValue(Executor.libIncludes);
		}
	}
	
	@Override
	public boolean performOk() {
		String excludedFilters = StringUtils.join(excludedTable.getFilterTexts(), LIBS_SEPARATOR);
		String includedFilters = StringUtils.join(includedTable.getFilterTexts(), LIBS_SEPARATOR);
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(ID);
		preferences.put(EXCLUDED_LIBS, excludedFilters);
		preferences.put(INCLUDED_LIBS, includedFilters);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			MessageDialogs.showErrorInUI("Error when saving preferences: " + e.getMessage());
			return false;
		}
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		pref.putValue(EXCLUDED_LIBS, excludedFilters);
		pref.putValue(INCLUDED_LIBS, includedFilters);
		return true;
	}
}
