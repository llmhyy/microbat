package microbat.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import sav.common.core.utils.CollectionUtils;

public class MysqlConnectionFactory {
	private static int dbSettingsVersion = -1;
	private static MysqlDataSource dataSource;
	
	public static Connection initilizeMysqlConnection() {
		try {
			if (!verifyDatasource()) {
				Connection conn = dataSource.getConnection();
				DbService.verifyDbTables(conn);
				
				return conn;
			} else {
				return dataSource.getConnection();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * return whether datasource is verified or not;
	 * */
	private static boolean verifyDatasource() throws SQLException {
		synchronized (DBSettings.class) {
			int dbVersion = DBSettings.getVersion();
			if (dbVersion == dbSettingsVersion) {
				return true; // verified!
			}
			// verify database
			dataSource = new MysqlDataSource();
			dataSource.setServerName(DBSettings.dbAddress);
			dataSource.setPort(DBSettings.dbPort);
			dataSource.setUser(DBSettings.username);
			dataSource.setPassword(DBSettings.password);
			String dbName = DBSettings.dbName;
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			try {
				conn = dataSource.getConnection();
				conn.setAutoCommit(true);
				rs = conn.getMetaData().getCatalogs();
				boolean exist = false;
				while(rs.next()) {
					String database = rs.getString(1);
					if (database.equals(dbName)) {
						exist = true;
						break;
					}
				}
				rs.close();
				if (!exist) {
					stmt = conn.createStatement();
					int row = stmt.executeUpdate("CREATE DATABASE " + dbName);
					if (row <= 0) {
						throw new SQLException("Cannot create database " + dbName);
					}
				}
				dataSource.setDatabaseName(dbName);
				conn.close();
				dbSettingsVersion = dbVersion;
				return false;
			} finally {
				DbService.closeDb(conn, CollectionUtils.<AutoCloseable>listOf(stmt, rs));
				if (conn != null) {
					conn.close();
				}
			}
		}
	}
	
	
	
	
}

	
