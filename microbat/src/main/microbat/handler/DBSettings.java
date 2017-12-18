package microbat.handler;

import static microbat.preference.DatabasePreference.*;
import org.eclipse.jface.preference.IPreferenceStore;

import microbat.Activator;

public class DBSettings {
	public String dbAddress = "localhost";
	public int dbPort = 3306;
	public String username = "root";
	public String password = "123456";
	public String dbName = "trace";
	
	static {
		updateFromPreference();
	}
	
	public static void updateFromPreference() {
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		dbAddress = pref.getString(HOST);
		dbPort = pref.getInt(PORT);
		dbName = pref.getString(DATABASE);
		username = pref.getString(USER_NAME);
		password = pref.getString(PASSWORD);
	}
}
