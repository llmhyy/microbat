package microbat.preference;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import microbat.Activator;
import microbat.sql.DBSettings;
import microbat.util.MessageDialogs;
import microbat.util.SWTFactory;

public class DatabasePreference extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String ID = "microbat.preference.database";
	public static final String HOST = "dbHost";
	public static final String PORT = "dbPort";
	public static final String DATABASE = "dbName";
	public static final String USER_NAME = "dbUserName";
	public static final String PASSWORD = "dbPassword";
	
	private StringFieldEditor hostField;
	private IntegerFieldEditor portField;
	private StringFieldEditor databaseNameField;
	private StringFieldEditor userNameField;
	private StringFieldEditor passwordField;
	
	@Override
	public void init(IWorkbench workbench) {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns = 1;
		contents.setLayout(layout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		SWTFactory.createLabel(contents, "MySql Database Configuration:", 1);
		Group group = SWTFactory.createGroup(contents, "", 2);
		hostField = new StringFieldEditor(HOST, "Host Name", group);
		portField = new IntegerFieldEditor(PORT, "Port", group);
		databaseNameField = new StringFieldEditor(DATABASE, "Database Name", group);
		userNameField = new StringFieldEditor(USER_NAME, "User Name", group);
		passwordField = new StringFieldEditor(PASSWORD, "Password", group);
		passwordField.getTextControl(group).setEchoChar('*');
		setDefaultValue();
		return contents;
	}
	
	private void setDefaultValue() {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		hostField.setStringValue(pref.getString(HOST));
		portField.setStringValue(pref.getString(PORT));
		databaseNameField.setStringValue(pref.getString(DATABASE));
		userNameField.setStringValue(pref.getString(USER_NAME));
		passwordField.setStringValue(pref.getString(PASSWORD));
	}
	
	@Override
	public boolean performOk() {
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(ID);
		preferences.put(HOST, hostField.getStringValue());
		preferences.putInt(PORT, portField.getIntValue());
		preferences.put(DATABASE, databaseNameField.getStringValue());
		preferences.put(USER_NAME, userNameField.getStringValue());
		preferences.put(PASSWORD, passwordField.getStringValue());
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
		DBSettings.updateFromPreference();
		return true;
	}

}
