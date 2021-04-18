package microbat.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.sqlite.SQLiteDataSource;

import microbat.Activator;
import microbat.util.IResourceUtils;
import sav.common.core.utils.CollectionUtils;

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
			transferSql();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		DbService.verifyDbTables(conn);
		return conn;
	}

	private static void transferSql() throws FileNotFoundException {
		// TODO Yuchen
		/**
		 * translate the autocrement, create a backup for mysql ddl, and use sqlite ddl.
		 */
		Set<String> expectedTables = new HashSet<String>(DbService.MICROBAT_TABLES);
		for(String tableName: expectedTables) {
			File file = new File(
					IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl/" + tableName + ".sql"));
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			
		}
	}
	
}
