package microbat.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import sav.common.core.utils.CollectionUtils;

public class MysqlConnectionFactory implements ConnectionFactory {
	private int dbSettingsVersion;
	private MysqlDataSource dataSource;
	
	MysqlConnectionFactory() {
		this.dbSettingsVersion = -1;
		this.dataSource = new MysqlDataSource();
		this.dataSource.setServerName(DBSettings.dbAddress);
		this.dataSource.setPort(DBSettings.dbPort);
		this.dataSource.setUser(DBSettings.username);
		this.dataSource.setPassword(DBSettings.password);
	}
	
	public Connection initializeConnection() throws SQLException {
		if (!verifyDatasource()) {
			Connection conn = dataSource.getConnection();
			DbService.verifyDbTables(conn);
			
			return conn;
		} else {
			return dataSource.getConnection();
		}
	}
	
	/**
	 * return whether datasource is verified or not;
	 * */
	private boolean verifyDatasource() throws SQLException {
		synchronized (DBSettings.class) {
			int dbVersion = DBSettings.getVersion();
			if (dbVersion == dbSettingsVersion) {
				return true; // verified!
			}
			// verify database
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
				this.dataSource.setDatabaseName(dbName);
				conn.close();
				this.dbSettingsVersion = dbVersion;
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

	
