package microbat.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import microbat.Activator;
import microbat.util.IResourceUtils;
import sav.common.core.utils.CollectionUtils;

public class DbService {
	private static final List<String> MICROBAT_TABLES;
	private static boolean verified = false;
	
	static {
		MICROBAT_TABLES = collectCreateScript();
	}
	
	public static List<String> collectCreateScript() {
		try {
			File ddlFolder = new File(IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl"));
			final List<String> tables = new ArrayList<>();
			ddlFolder.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".sql") || name.endsWith(".SQL")) {
						tables.add(name.substring(0,
								name.length() - 4 /* ".sql".length */));
						return true;
					}
					return false;
				}
			});
			return tables;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	public Connection getConnection() throws SQLException {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setServerName(DBSettings.dbAddress);
		dataSource.setPort(DBSettings.dbPort);
		dataSource.setUser(DBSettings.username);
		dataSource.setPassword(DBSettings.password);
		dataSource.setDatabaseName(DBSettings.dbName);
		Connection conn = dataSource.getConnection();
		verifyDatabase(conn);
		return conn;
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
	
	protected void verifyDatabase(Connection conn) throws SQLException {
		if (verified) {
			return;
		}
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
		Set<String> expectedTables = new HashSet<String>(MICROBAT_TABLES);
		while (rs.next()) {
			expectedTables.remove(rs.getString(3));
		}
		if (!expectedTables.isEmpty() || DBSettings.forceRunCreateScript) {
			System.out.println("Missing tables: " + expectedTables.toString());
			String s;
			StringBuffer sb = new StringBuffer();
			try {
				for (String tableName : MICROBAT_TABLES) {
					FileReader fr = new FileReader(new File(
							IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl/" + tableName + ".sql")));
					BufferedReader br = new BufferedReader(fr);
					while ((s = br.readLine()) != null) {
						if ("USE trace".equals(s)) {
							s = s.replace("trace", DBSettings.dbName);
						}
						sb.append(s);
					}
					br.close();
				}
				String[] inst = sb.toString().split(";");
				Statement st = conn.createStatement();
				conn.setAutoCommit(false);
				for (int i = 0; i < inst.length; i++) {
					if (!inst[i].trim().equals("")) {
						st.executeUpdate(inst[i]);
						System.out.println(">>" + inst[i]);
					}
				}
				conn.commit();
				verified = true;
			} catch (IOException e) {
				throw new SQLException(e);
			}
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

	public void closeDb(Connection connection, List<Statement> stmts, ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		for (Statement stmt : CollectionUtils.nullToEmpty(stmts)) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
