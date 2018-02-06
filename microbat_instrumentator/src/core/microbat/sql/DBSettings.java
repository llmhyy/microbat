package microbat.sql;

public class DBSettings {
	public static String dbAddress = "localhost";
	public static int dbPort = 3306;
	public static String username = "root";
	public static String password = "123456";
	public static String dbName = "microbattest";
	public static boolean enableAutoUpdateDb = true;
	private static int version = 0; //keep track for the update
	
	public static int getVersion() {
		return version;
	}
}
