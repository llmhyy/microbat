package microbat.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import microbat.Activator;
import microbat.instrumentation.filter.CodeRangeEntry;
import microbat.model.Entry;
import microbat.util.MessageDialogs;
import microbat.util.SWTFactory;
import sav.common.core.utils.StringUtils;

/**
 * 
 * @author Song Xuezhi 22/2 2020
 * 
 */
public class ExecutionRangePreference extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String ID = "microbat.preference.executionrange";
	private static final String ENTRYS = "entrys";
	private static final String PARTIAL_RECORDING = "enablePartialRecordinng";
	public static final String LIBS_SEPARATOR = ";";

	private ExecutionRangeTablePanel executionRangeTable;
	private Button enablePartialRecordingButton;

	@Override
	public Point computeSize() {
		return super.computeSize();
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout());
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		SWTFactory.createLabel(contents, "Execution Range Configuration:", 1);
		executionRangeTable = new ExecutionRangeTablePanel(contents, "entry");
		enablePartialRecordingButton = new Button(contents, SWT.CHECK);
		enablePartialRecordingButton.setText("Enable partial recording function");
		setDefaultValue();
		return contents;
	}

	private void setDefaultValue() {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		executionRangeTable.setValue(getEntrys());
		enablePartialRecordingButton.setSelection(pref.getBoolean(PARTIAL_RECORDING));
	}

	public static List<Entry> getEntrys() {
		List<Entry> result = new ArrayList<>();
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		String entrys = pref.getString(ENTRYS);
		if (!StringUtils.isEmpty(entrys)) {
			String[] filters = entrys.split(LIBS_SEPARATOR);
			for (int i = 0; i < filters.length; i++) {
				String[] cloumns = filters[i].split(Entry.COLUMN_SEPARATOR);
				if (cloumns.length == 3) {
					Entry entry = new Entry();
					entry.setClassName(cloumns[0]);
					entry.setStartLine(Integer.valueOf(cloumns[1]));
					entry.setEndLine(Integer.valueOf(cloumns[2]));
					result.add(entry);
				}
			}
		}
		return result;
	}

	public static boolean isEnablePartialRecording() {
		boolean result = false;
		result = Activator.getDefault().getPreferenceStore().getBoolean(PARTIAL_RECORDING);
		return result;
	}

	public static List<CodeRangeEntry> getCodeRangeEntrys() {
		List<CodeRangeEntry> codeRangeEntries = new ArrayList<>();
		List<Entry> entries = getEntrys();
		if (entries.size() > 0 && isEnablePartialRecording()) {
			for (Entry entry : entries) {
				CodeRangeEntry codeRangeEntry = new CodeRangeEntry(entry.getClassName(), entry.getStartLine(),
						entry.getEndLine());
				codeRangeEntries.add(codeRangeEntry);
			}
		}
		return codeRangeEntries;
	}

	@Override
	public boolean performOk() {
		String entrys = StringUtils.join(executionRangeTable.getEntrys(), LIBS_SEPARATOR);
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(ID);
		preferences.put(ENTRYS, entrys);
		preferences.putBoolean(PARTIAL_RECORDING, enablePartialRecordingButton.getSelection());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			MessageDialogs.showErrorInUI("Error when saving preferences: " + e.getMessage());
			return false;
		}
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		pref.putValue(ENTRYS, entrys);
		pref.putValue(PARTIAL_RECORDING, String.valueOf(enablePartialRecordingButton.getSelection()));
		return true;
	}

}
