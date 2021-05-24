/**
 * 
 */
package microbat.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.sqlite.SQLiteDataSource;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import sav.common.core.utils.CollectionUtils;
;

/**
 * @author knightsong
 *
 */
public class SqliteServer {

	// Sqllite db file path
	// public static String dbFilePath = "microbat_test.db";
	protected static final int BATCH_SIZE = 1000;
	private static SQLiteDataSource dataSource = new SQLiteDataSource();

	public SqliteServer(String dbPath) {
//		SqliteServer.dbFilePath = dbPath;
		dataSource.setUrl("jdbc:sqlite:" + dbPath);
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public void rollback(Connection conn) {
		try {
			if (conn != null) {
				conn.rollback();
			}
		} catch (SQLException e1) {
			// ignore
		}
	}
	
	public void closeDb(Connection connection, List<AutoCloseable> closableList) {
		for (AutoCloseable obj : CollectionUtils.nullToEmpty(closableList)) {
			if (obj != null) {
				try {
					obj.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}
	
	public String getUUID(){
        UUID uuid=UUID.randomUUID();
        String uuidStr=uuid.toString();
        return uuidStr;
	}
	
//	protected int getFirstGeneratedIntCol(PreparedStatement ps) throws SQLException {
//		int id = -1;
//		try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
//			if (generatedKeys.next()) {
//				id = generatedKeys.getInt("last_insert_rowid()");
//			} else {
//				throw new SQLException("Update failed, no ID obtained.");
//			}
//		}
//		if (id < 0) {
//			throw new SQLException("Insert trace failed, no traceId obtained.");
//		}
//		return id;
//	}
//	
//	protected List<Integer> getGeneratedIntIds(PreparedStatement ps) throws SQLException {
//		List<Integer> generatedIds = new ArrayList<>();
//		try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
//			while (generatedKeys.next()) {
//				generatedIds.add(generatedKeys.getInt("last_insert_rowid()"));
//			}
//		}
//		return generatedIds;
//	}
}
