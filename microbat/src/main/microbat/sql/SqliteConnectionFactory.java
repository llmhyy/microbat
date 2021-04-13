package microbat.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteDataSource;

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
		
		transferSql();
		
		DbService.verifyDbTables(conn);
		return conn;
	}

	private static void transferSql() {
		// TODO Yuchen
		//transfer the sql script into sqlite3 script
	}
	
}
