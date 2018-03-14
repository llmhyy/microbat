package microbat.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import microbat.Activator;
import microbat.codeanalysis.runtime.Executor;
import microbat.util.MessageDialogs;
import microbat.util.SWTFactory;
import sav.common.core.utils.StringUtils;

public class AnalysisScopePreference extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String EXCLUDED_LIBS = "excludedLibs";
	public static final String INCLUDED_LIBS = "includedLibs";
	public static final String LIBS_SEPARATOR = ";";
	private static final String ID = "microbat.preference.analysisScope";
	private AnalysisScopesTablePanel excludedTable;
	private AnalysisScopesTablePanel includedTable;
	
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Label hintLbl = SWTFactory.createWrapLabel(parent,
				"By default, only classes in source code are included, while all other classes in references are excluded. \n"
				+ "More packages and classes to be analyzed as well as exclude filters can be added "
				+ "in the tables below.",
				1, 300);
		hintLbl.setFont(JFaceResources.getFontRegistry().getItalic(hintLbl.getFont().toString()));
		SashForm content = new SashForm(parent, SWT.VERTICAL);
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		includedTable = new AnalysisScopesTablePanel(content, "Add more packages/types to analyze:", false);
		excludedTable = new AnalysisScopesTablePanel(content, "Add more exclude filters", true);
		setDefaultValues();
		return content;
	}
	
	private void setDefaultValues() {
		includedTable.setValue(getIncludedLibs());
		excludedTable.setValue(getExcludedLibs());
	}
	
	public static String[] getIncludedLibs() {
		return getFilterLibs(INCLUDED_LIBS);
	}
	
	public static List<String> getIncludedLibList(){
		List<String> list = new ArrayList<>();
		String[] array = getIncludedLibs();
		for(String str: array){
			list.add(str);
		}
		return list;
	}
	
	public static String[] getExcludedLibs() {
		return getFilterLibs(EXCLUDED_LIBS);
	}
	
	public static List<String> getExcludedLibList(){
		List<String> list = new ArrayList<>();
		String[] array = getExcludedLibs();
		for(String str: array){
			list.add(str);
		}
		return list;
	}
	
	private static String[] getFilterLibs(String key) {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		String excludedStr = pref.getString(key);
		if (!StringUtils.isEmpty(excludedStr)) {
			String[] filters = excludedStr.split(LIBS_SEPARATOR);
			return filters;
		}
		return new String[0];
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
		String[] excludePatterns = Executor.deriveLibExcludePatterns();
		
		Executor.setLibExcludes(excludePatterns);
		String[] includePatterns = AnalysisScopePreference.getIncludedLibs();
		for(int i=0; i<includePatterns.length; i++){
			String includePattern = includePatterns[i];
			includePatterns[i] = includePattern.replace("\\", "");
		}
		Executor.libIncludes = includePatterns;
//		System.out.println(StringUtils.join("\n", (Object[])strs));
		return true;
	}
}
