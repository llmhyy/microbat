package microbat.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import microbat.Activator;
import microbat.handler.xml.VarValueXmlWriter;
import microbat.model.BreakPoint;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.IResourceUtils;
import microbat.util.MessageDialogs;
import microbat.util.Settings;
import microbat.util.WorkbenchUtils;
import microbat.views.MicroBatViews;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StringUtils;

public class StoreTraceHandler extends AbstractHandler {
	private static final int READ = 1;
	private static final int WRITE = 2;
	private static final List<String> MICROBAT_TABLES = Arrays.asList("Location", "MendingRecord", "Regression", "RegressionMatch",
			"Step", "StepVariableRelation");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Trace trace = MicroBatViews.getTraceView().getTrace();
		Connection conn = null;
		List<Statement> stmts = new ArrayList<Statement>();
		ResultSet rs = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			String traceId = insertTrace(trace, conn, stmts);
			insertSteps(traceId, trace.getExecutionList(), conn, stmts);
			insertStepVariableRelation(trace, traceId, conn, stmts);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				// ignore
			}
			MessageDialogs.showErrorInUI(StringUtils.spaceJoin("Cannot store trace: ", e.getMessage()));
		} finally {
			closeDb(conn, stmts, rs);
		}
		
		System.out.println("test");
		return null;
	}
	
	private String insertTrace(Trace trace, Connection conn, List<Statement> stmts)
			throws SQLException {
		PreparedStatement ps;
		String sql = "insert into trace (launch_class, project_name, project_version, bug_id, generated_time) "
				+ "values (?, ?, ?, ?, ?)";
		ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int idx = 1;
		ps.setString(idx++, trace.getAppJavaClassPath().getLaunchClass());
		ps.setString(idx++, Settings.projectName);
		ps.setString(idx++, null);
		ps.setString(idx++, null);
		ps.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));
		stmts.add(ps);
		ps.execute();
		try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
			if (generatedKeys.next()) {
				return generatedKeys.getString(1);
			} else {
				throw new SQLException("Update failed, no ID obtained.");
			}
		}
	}
	
	private void insertSteps(String traceId, List<TraceNode> exectionList, Connection conn,
			List<Statement> stmts) throws SQLException {
		String sql = "INSERT INTO step (trace_id, step_order, control_dominator, step_in, step_over, invocation_parent, loop_parent,"
				+ "location_id, read_vars, written_vars) VALUES (?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		Set<BreakPoint> locations = new HashSet<>();
		for (int i = 0; i < exectionList.size(); i++) {
			TraceNode node = exectionList.get(i);
			int idx = 1;
			ps.setString(idx++, traceId);
			ps.setInt(idx++, node.getOrder());
			setNodeOrder(ps, idx++, node.getControlDominator());
			setNodeOrder(ps, idx++, node.getStepInNext());
			setNodeOrder(ps, idx++, node.getStepOverNext());
			setNodeOrder(ps, idx++, node.getInvocationParent());
			setNodeOrder(ps, idx++, node.getLoopParent());
			ps.setString(idx++, node.getBreakPoint().getId());
			ps.setString(idx++, generateXmlContent(node.getReadVariables()));
			ps.setString(idx++, generateXmlContent(node.getWrittenVariables()));
			ps.addBatch();
			locations.add(node.getBreakPoint());
		}
		ps.executeBatch();
		stmts.add(ps);
		insertLocation(traceId, locations, conn, stmts);
		
	}

	private String generateXmlContent(List<VarValue> varValues) {
		if (CollectionUtils.isEmpty(varValues)) {
			return null;
		}
		return VarValueXmlWriter.generateXmlContent(varValues);
	}
	
	private void insertStepVariableRelation(Trace trace, String traceId, Connection conn, List<Statement> stmts)
			throws SQLException {
		String sql = "INSERT INTO stepVariableRelation (var_id, trace_id, step_order, rw) VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		for (StepVariableRelationEntry entry : trace.getStepVariableTable().values()) {
			for (TraceNode node : entry.getProducers()) {
				int idx = 1;
				ps.setString(idx++, entry.getVarID());
				ps.setString(idx++, traceId);
				ps.setInt(idx++, node.getOrder());
				ps.setInt(idx++, WRITE);
				ps.addBatch();
			}
			for (TraceNode node : entry.getConsumers()) {
				int idx = 1;
				ps.setString(idx++, entry.getVarID());
				ps.setString(idx++, traceId);
				ps.setInt(idx++, node.getOrder());
				ps.setInt(idx++, READ);
				ps.addBatch();
			}
		}
		ps.executeBatch();
		stmts.add(ps);
	}

	private void setNodeOrder(PreparedStatement ps, int idx, TraceNode node) throws SQLException {
		if (node != null) {
			ps.setInt(idx, node.getOrder());
		} else {
			ps.setNull(idx, java.sql.Types.INTEGER);
		}
	}
	
	private void insertLocation(String traceId, Set<BreakPoint> locations, Connection conn, List<Statement> stmts)
			throws SQLException {
		String sql = "INSERT INTO location (trace_id, location_id, class_name, line_number, is_conditional, is_return) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		for (BreakPoint location : locations) {
			int idx = 1;
			ps.setString(idx++, traceId);
			ps.setString(idx++, location.getId());
			ps.setString(idx++, location.getDeclaringCompilationUnitName());
			ps.setInt(idx++, location.getLineNumber());
			ps.setBoolean(idx++, location.isConditional());
			ps.setBoolean(idx++, location.isReturnStatement());
			ps.addBatch();
		}
		ps.executeBatch();
		stmts.add(ps);
	}
	
	private Connection getConnection() throws SQLException {
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
	
	private void verifyDatabase(Connection conn) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
		Set<String> expectedTables = new HashSet<String>(MICROBAT_TABLES);
		while (rs.next()) {
			expectedTables.remove(rs.getString(3));
		}
		if (!expectedTables.isEmpty()) {
			/**/
			System.out.println("Missing tables: " + expectedTables.toString());
			boolean confirmed = MessageDialogs.warningConfirm(WorkbenchUtils.getActiveWorkbenchWindow().getShell(),
					String.format("Missing tables: %s. Run sql script to create tables?", expectedTables.toString()));
			if (confirmed) {
				String s;
				StringBuffer sb = new StringBuffer();
				try {
					for (String tableName : expectedTables) {
						FileReader fr = new FileReader(new File(
								IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, tableName + ".sql")));
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
				} catch (IOException e) {
					throw new SQLException(e);
				}
			}
		}
	}

	protected void closeDb(Connection connection, List<Statement> stmts, ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		for (Statement stmt : stmts) {
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
