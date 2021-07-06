package microbat.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import microbat.Activator;
import microbat.util.IResourceUtils;
import sav.common.core.utils.CollectionUtils;

public class DbService {
	protected static final int BATCH_SIZE = 1000;
	public static final List<String> MICROBAT_TABLES;

	static {
		MICROBAT_TABLES = collectDbTables();
	}

	public static List<String> collectDbTables() {
		try {
			File ddlFolder = new File(IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl"));
			final List<String> tables = new ArrayList<>();
			ddlFolder.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".sql") || name.endsWith(".SQL")) {
						String tableName = name.substring(0, name.length() - 4 /* ".sql".length */);
						String os = System.getProperty("os.name");
						if (os.toLowerCase().contains("win")) {
							tableName = tableName.toLowerCase();
						}
						tables.add(tableName);
						return true;
					}
					return false;
				}
			});
			return tables;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public static Connection getConnection() throws SQLException {
		// each retrieval will close the connection
		ConnectionFactory factory;
		switch (DBSettings.DMBS_TYPE) {
		case DBSettings.MYSQL_DBMS:
			factory = new MysqlConnectionFactory();
			break;
		case DBSettings.SQLITE3_DBMS:
			factory = new SqliteConnectionFactory();
			break;
		default:
			throw new SQLException("we yet support dbms type other than mysql and sqlite3");
		}

		return factory.initializeConnection();
	}

	public static void rollback(Connection conn) {
		try {
			if (conn != null) {
				conn.rollback();
			}
		} catch (SQLException e1) {
			// ignore
		}
	}

	public static Set<String> findMissingTables(Connection conn) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" });
		Set<String> expectedTables = new HashSet<String>(DbService.MICROBAT_TABLES);
		while (rs.next()) {
			String rsString = rs.getString(3);
			Iterator<String> iterator = expectedTables.iterator();
			while (iterator.hasNext()) {
				String table = iterator.next();
				if (table.toLowerCase().equals(rsString.toLowerCase())) {
					iterator.remove();
				}
			}
		}
		return expectedTables;
	}

	/**
	 * Check table names against ddl file and run create table scripts if table is not found
	 * @param conn Connection to database
	 * @throws SQLException
	 */
	public static void verifyDbTables(Connection conn) throws SQLException {
		Set<String> expectedTables = findMissingTables(conn);
		if (!expectedTables.isEmpty() && DBSettings.enableAutoUpdateDb) {
			System.out.println("Missing tables: " + expectedTables.toString());
			StringBuffer sb = new StringBuffer();
			try {
				for (String tableName : DbService.MICROBAT_TABLES) {
					readSqlScriptFile(sb, tableName);
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
			} catch (IOException e) {
				throw new SQLException(e);
			}
		}
	}

	private static void readSqlScriptFile(StringBuffer sb, String tableName) throws FileNotFoundException, IOException {
		String s;
		File file = new File(IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "ddl/" + tableName + ".sql"));
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		boolean check = CollectionUtils.existIn(tableName, "step", "mutationfile");
		StringBuilder newContent = new StringBuilder();
		boolean update = false;
		while ((s = br.readLine()) != null) {
			if (check) {
				int l = s.length();
				s = s.replace("read_vars TEXT", "read_vars MEDIUMTEXT");
				s = s.replace("written_vars TEXT", "written_vars MEDIUMTEXT");
				s = s.replace("mutation_file BLOB", "mutation_file MEDIUMBLOB");
				if (s.length() != l) {
					update = true;
				}
				newContent.append(s).append("\n");
			}
			sb.append(s);
		}
		br.close();
		fr.close();
		if (update) {
			FileWriter fw = new FileWriter(file, false);
			fw.write(newContent.toString());
			fw.close();
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

	public static void closeDb(Connection connection, List<AutoCloseable> closableList) {
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
