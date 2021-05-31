package microbat.sql;

import static microbat.preference.DatabasePreference.*;
import org.eclipse.jface.preference.IPreferenceStore;

import microbat.Activator;

public class DBSettings {
	
	public static final int SQLITE3_DBMS = 0;
	public static final int MYSQL_DBMS = 1;
	
	public static String dbAddress = "localhost";
	public static int dbPort = 3306;
	public static String username = "root";
	public static String password = "123456";
	public static String dbName = "microbat";
	public static String dbPath = "microbat.db";
	public static int DMBS_TYPE = SQLITE3_DBMS;
	public static String USE_DB = "false";
	public static boolean enableAutoUpdateDb = true;
	private static int version = -1; //keep track for the update
	
	static {
		updateFromPreference();
	}
	
	public static void updateFromPreference() {
		synchronized (DBSettings.class) {
			IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
			dbAddress = pref.getString(HOST);
			dbPort = pref.getInt(PORT);
			dbName = pref.getString(DATABASE);
			username = pref.getString(USER_NAME);
			password = pref.getString(PASSWORD);
			DMBS_TYPE = pref.getInt(DBMS);
			USE_DB = pref.getString(IS_STARTDB);
			dbPath = pref.getString(DBPATH);
			version++;
		}
	}
	
	public static int getVersion() {
		return version;
	}
}
