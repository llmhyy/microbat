package microbat.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import sav.common.core.utils.CollectionUtils;

class DbService {
	protected static final int BATCH_SIZE = 1000;
	private static MysqlDataSource dataSource;
	
	static {
		dataSource = new MysqlDataSource();
		dataSource.setServerName(DBSettings.dbAddress);
		dataSource.setPort(DBSettings.dbPort);
		dataSource.setUser(DBSettings.username);
		dataSource.setPassword(DBSettings.password);
		dataSource.setDatabaseName(DBSettings.dbName);
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
	
	protected int countNumberOfRows(ResultSet rs) throws SQLException {
		if (rs == null) {
			return 0;
		}
		try {
			rs.last();
			return rs.getRow();
		} finally {
			rs.beforeFirst();
		}
	}
	
	protected int getFirstGeneratedIntCol(PreparedStatement ps) throws SQLException {
		int id = -1;
		try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
			if (generatedKeys.next()) {
				id = generatedKeys.getInt("GENERATED_KEY");
			} else {
				throw new SQLException("Update failed, no ID obtained.");
			}
		}
		if (id < 0) {
			throw new SQLException("Insert trace failed, no traceId obtained.");
		}
		return id;
	}
	
	protected List<Integer> getGeneratedIntIds(PreparedStatement ps) throws SQLException {
		List<Integer> generatedIds = new ArrayList<>();
		try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
			while (generatedKeys.next()) {
				generatedIds.add(generatedKeys.getInt("GENERATED_KEY"));
			}
		}
		return generatedIds;
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
}
