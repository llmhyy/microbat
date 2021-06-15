package microbat.sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.sqlite.SQLiteDataSource;

import microbat.Activator;
import microbat.util.IResourceUtils;
import sav.common.core.SavRtException;

public class SqliteConnectionFactory implements ConnectionFactory {
	private SQLiteDataSource dataSource = new SQLiteDataSource();
	
	public SqliteConnectionFactory() {
		this.dataSource = new SQLiteDataSource();
	}

	public Connection initializeConnection() throws SQLException {
		dataSource.setUrl("jdbc:sqlite:" + DBSettings.dbPath);
		return getConnection();
	}

	private Connection getConnection() throws SQLException {
		Connection conn = dataSource.getConnection();
		conn.setAutoCommit(true);

		Set<String> missingTables = DbService.findMissingTables(conn);
		for (String table : missingTables) {
			transferSql(conn, table);
		}

		DbService.verifyDbTables(conn);
		return conn;
	}

	/**
	 * translate the auto-increment, create a backup for mysql ddl, and use sqlite ddl.
	 */
	private static void transferSql(Connection conn, String tableName) throws SQLException {
		try {
			Path file = Paths.get(IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl/" + tableName + ".sql"));
			String sqlCreateTableScript = new String(Files.readAllBytes(file));
			Statement st = conn.createStatement();
			st.execute(convertToSqlite(sqlCreateTableScript));	
		} catch (IOException | SavRtException e) {
			e.printStackTrace();
		}
	}

	private static String convertToSqlite(String sql) {
		String ret = sql;
		ret = ret.replace("AUTO_INCREMENT", "");
		ret = ret.replace("VARCHAR", "TEXT");
//		ret = ret.replace("TIMESTAMP", "INTEGER");
		ret = ret.replace("BOOL", "INTEGER");
		return ret;
	}
}
