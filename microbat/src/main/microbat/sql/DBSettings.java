package microbat.sql;

import static microbat.preference.DatabasePreference.*;
import org.eclipse.jface.preference.IPreferenceStore;

import microbat.Activator;

public class DBSettings {
	public static String dbAddress = "localhost";
	public static int dbPort = 3306;
	public static String username = "root";
	public static String password = "123456";
	public static String dbName = "trace";
	public static boolean forceRunCreateScript = false;
	
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
