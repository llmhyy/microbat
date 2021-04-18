package microbat.sql;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.sqlite.SQLiteDataSource;

import microbat.Activator;
import microbat.util.IResourceUtils;

public class SqliteConnectionFactory {
	private static SQLiteDataSource dataSource = new SQLiteDataSource();

	public static Connection initilizeMysqlConnection() {
		dataSource.setUrl("jdbc:sqlite:" + DBSettings.dbPath);
		try {
			return getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static Connection getConnection() throws SQLException {
		
		Connection conn = dataSource.getConnection();
		
		try {
			transferSql(conn);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		DbService.verifyDbTables(conn);
		return conn;
	}

	private static void transferSql(Connection conn) throws FileNotFoundException {
		// TODO Yuchen
		/**
		 * translate the autocrement, create a backup for mysql ddl, and use sqlite ddl.
		 */
		Set<String> expectedTables = new HashSet<String>(DbService.MICROBAT_TABLES);
		for(String tableName: expectedTables) {
			transferSql(conn, tableName);
		}
		System.out.println("created table");
	}


	private static void transferSql(Connection conn, String tableName) {
		Path file = Paths.get(
				IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl/" + tableName + ".sql"));
		try {
			String sqlCreateTableScript = new String(Files.readAllBytes(file));
			// remove instances of "AUTO_INCREMENT"
			Statement st = conn.createStatement();
			st.execute(sqlCreateTableScript.replace("AUTO_INCREMENT", ""));
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
}
