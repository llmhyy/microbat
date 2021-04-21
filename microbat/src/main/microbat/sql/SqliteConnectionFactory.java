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
import sav.common.core.SavRtException;

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

		Set<String> missingTables = DbService.findMissingTables(conn);
		for (String table : missingTables) {
			transferSql(conn, table);
		}

		DbService.verifyDbTables(conn);
		return conn;
	}

	/**
	 * translate the autocrement, create a backup for mysql ddl, and use sqlite ddl.
	 */
	// private static void transferSql(Connection conn) throws FileNotFoundException
	// {
	// // check if tables already exist
	// Set<String> expectedTables = new HashSet<String>(DbService.MICROBAT_TABLES);
	// for(String tableName: expectedTables) {
	// transferSql(conn, tableName);
	// }
	// // System.out.println("created table");
	// }

	private static void transferSql(Connection conn, String tableName) {
		try {
			Path file = Paths.get(IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl/" + tableName + ".sql"));
			String sqlCreateTableScript = new String(Files.readAllBytes(file));
			// remove instances of "AUTO_INCREMENT"
			Statement st = conn.createStatement();
			st.execute(convertToSqlite(sqlCreateTableScript));
		} catch (IOException | SQLException | SavRtException e) {
			e.printStackTrace();
		}
	}

	private static String convertToSqlite(String sql) {
		String ret = sql;
		ret = ret.replace("AUTO_INCREMENT", "");
		ret = ret.replace("VARCHAR", "TEXT");
		ret = ret.replace("TIMESTAMP", "INTEGER");
		ret = ret.replace("BOOL", "INTEGER");
		return ret;
	}
}
