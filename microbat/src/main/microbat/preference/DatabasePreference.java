package microbat.preference;

import java.io.File;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import microbat.Activator;
import microbat.sql.DBSettings;
import microbat.trace.Reader;
import microbat.util.MessageDialogs;
import microbat.util.MicroBatUtil;
import microbat.util.SWTFactory;

public class DatabasePreference extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String ID = "microbat.preference.database";
	public static final String HOST = "dbHost";
	public static final String PORT = "dbPort";
	public static final String DATABASE = "dbName";
	public static final String USER_NAME = "dbUserName";
	public static final String PASSWORD = "dbPassword";
	public static final String IS_STARTDB = "startdb";
	public static final String DBMS = "dbms";
	public static final String DBPATH = "dbPath`";

	private StringFieldEditor hostField;
	private IntegerFieldEditor portField;
	private StringFieldEditor databaseNameField;
	private StringFieldEditor userNameField;
	private StringFieldEditor passwordField;
	private Combo dataBaseDropDown;
	private Button startWithSQL;
	private DirectoryFieldEditor sqliteDBPath;

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		contents.setLayout(layout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		startWithSQL = SWTFactory.createCheckbox(contents, "Start with SQL", 1);
		dataBaseDropDown = SWTFactory.creatDropdown(contents);
		dataBaseDropDown.add(Reader.SQLITE3.toString());
		dataBaseDropDown.add(Reader.MYSQL.toString());
		dataBaseDropDown.select(0);

		SWTFactory.createLabel(contents, "Database Configuration:", 2);
		Group group = SWTFactory.createGroup(contents, "", 3);
		hostField = new StringFieldEditor(HOST, "Host Name", group);
		portField = new IntegerFieldEditor(PORT, "Port", group);
		databaseNameField = new StringFieldEditor(DATABASE, "Database Name", group);
		userNameField = new StringFieldEditor(USER_NAME, "User Name", group);
		passwordField = new StringFieldEditor(PASSWORD, "Password", group);
		passwordField.getTextControl(group).setEchoChar('*');

		SWTFactory.createLabel(contents, "Sqlite Configuration:", 3);
		Group sqliteGroup = SWTFactory.createGroup(contents, "", 4);
		sqliteDBPath = new DirectoryFieldEditor(DBPATH, "Database path", sqliteGroup);
		setDefaultValue();
		return contents;
	}

	public static Reader getReader() {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		if (pref.getBoolean(IS_STARTDB)) {
			return pref.getInt(DBMS) == 1 ? Reader.MYSQL : Reader.SQLITE3;
		} else {
			return Reader.FILE;
		}
	}

	public static File getDBFile() {
		String filePath = Activator.getDefault().getPreferenceStore().getString(DBPATH);
		
		if (filePath.trim().equals("") || filePath == null) {
			System.err.println("need to specify the db file location");
		} else {
			return new File(filePath);
		}
		
		return null;
	}

	private void setDefaultValue() {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		hostField.setStringValue(pref.getString(HOST));
		portField.setStringValue(pref.getString(PORT));
		databaseNameField.setStringValue(pref.getString(DATABASE));
		userNameField.setStringValue(pref.getString(USER_NAME));
		passwordField.setStringValue(pref.getString(PASSWORD));
		startWithSQL.setSelection(pref.getBoolean(IS_STARTDB));
		dataBaseDropDown.select(pref.getInt(DBMS));
		sqliteDBPath.setStringValue(pref.getString(DBPATH));
	}

	@Override
	public boolean performOk() {
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(ID);
		preferences.put(HOST, hostField.getStringValue());
		String portString = portField.getStringValue();
		try {
			if (portString != null && !portString.trim().equals("")) {
				preferences.putInt(PORT, Integer.valueOf(portString));
			}
		} catch (Exception e) {
			MessageDialogs.showErrorInUI("Port must be number.");
		}
		preferences.put(DATABASE, databaseNameField.getStringValue());
		preferences.put(USER_NAME, userNameField.getStringValue());
		preferences.put(PASSWORD, passwordField.getStringValue());
		preferences.putBoolean(IS_STARTDB, startWithSQL.getSelection());
		preferences.putInt(DBMS, dataBaseDropDown.getSelectionIndex());
		preferences.put(DBPATH, sqliteDBPath.getStringValue());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			MessageDialogs.showErrorInUI("Error when saving preferences: " + e.getMessage());
			return false;
		}
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		pref.putValue(HOST, hostField.getStringValue());
		pref.putValue(PORT, portField.getStringValue());
		pref.putValue(DATABASE, databaseNameField.getStringValue());
		pref.putValue(USER_NAME, userNameField.getStringValue());
		pref.putValue(PASSWORD, passwordField.getStringValue());
		pref.putValue(IS_STARTDB, String.valueOf(startWithSQL.getSelection()));
		pref.putValue(DBMS, String.valueOf(dataBaseDropDown.getSelectionIndex()));
		pref.putValue(DBPATH, String.valueOf(sqliteDBPath.getStringValue()));

		DBSettings.updateFromPreference();
		return true;
	}
}
