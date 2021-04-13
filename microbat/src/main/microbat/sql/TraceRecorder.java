package microbat.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import microbat.handler.xml.VarValueXmlWriter;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.utils.CollectionUtils;

public class TraceRecorder extends DbService {
	public static final int READ = 1;
	public static final int WRITE = 2;
	
	public void storeTrace(Trace trace) throws SQLException {
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<AutoCloseable>();
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			insertTrace(trace, conn, closables);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			rollback(conn);;
			throw e;
		} finally {
			closeDb(conn, closables);
		}
	}
	
	public int insertTrace(Trace trace, Connection conn, List<AutoCloseable> closables)
			throws SQLException {
		return insertTrace(trace, null, null, null, null, conn, closables);
	}
	
	public int insertTrace(Trace trace, String projectName, String projectVersion, String launchClass,
			String launchMethod, Connection conn, List<AutoCloseable> closables) throws SQLException {
		PreparedStatement ps;
		String sql = "INSERT INTO Trace (project_name, project_version, "
				+ "launch_class, launch_method, generated_time, is_multithread) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		closables.add(ps);
		int idx = 1;
		ps.setString(idx++, projectName);
		ps.setString(idx++, projectVersion);
		ps.setString(idx++, launchClass);
		ps.setString(idx++, launchMethod);
		ps.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));
		ps.setBoolean(idx++, false);
		ps.execute();
		int traceId = getFirstGeneratedIntCol(ps);
		insertSteps(traceId, trace.getExecutionList(), conn, closables);
//		insertStepVariableRelation(trace, traceId, conn, closables);
		return traceId;
	}
	
	private void insertSteps(int traceId, List<TraceNode> exectionList, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String sql = "INSERT INTO Step (trace_id, step_order, control_dominator, step_in, step_over, invocation_parent, loop_parent,"
				+ "location_id, read_vars, written_vars) VALUES (?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		Map<TraceNode, Integer> locationIdMap = insertLocation(traceId, exectionList, conn, closables);
		int count = 0;
		for (int i = 0; i < exectionList.size(); i++) {
			TraceNode node = exectionList.get(i);
			int idx = 1;
			ps.setInt(idx++, traceId);
			ps.setInt(idx++, node.getOrder());
			setNodeOrder(ps, idx++, node.getControlDominator());
			setNodeOrder(ps, idx++, node.getStepInNext());
			setNodeOrder(ps, idx++, node.getStepOverNext());
			setNodeOrder(ps, idx++, node.getInvocationParent());
			setNodeOrder(ps, idx++, node.getLoopParent());
			ps.setInt(idx++, locationIdMap.get(node));
			ps.setString(idx++, generateXmlContent(node.getReadVariables()));
			ps.setString(idx++, generateXmlContent(node.getWrittenVariables()));
			ps.addBatch();
			if (++count == BATCH_SIZE) {
				ps.executeBatch();
				count = 0;
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}
	}

	protected String generateXmlContent(List<VarValue> varValues) {
		if (CollectionUtils.isEmpty(varValues)) {
			return null;
		}
		return VarValueXmlWriter.generateXmlContent(varValues);
	}
	
//	private void insertStepVariableRelation(Trace trace, int traceId, Connection conn, List<AutoCloseable> closables)
//			throws SQLException {
//		String sql = "INSERT INTO StepVariableRelation (var_id, trace_id, step_order, rw) VALUES (?, ?, ?, ?)";
//		PreparedStatement ps = conn.prepareStatement(sql);
//		closables.add(ps);
//		int count = 0;
//		for (StepVariableRelationEntry entry : trace.getStepVariableTable().values()) {
//			for (TraceNode node : entry.getProducers()) {
//				int idx = 1;
//				ps.setString(idx++, entry.getVarID());
//				ps.setInt(idx++, traceId);
//				ps.setInt(idx++, node.getOrder());
//				ps.setInt(idx++, WRITE);
//				ps.addBatch();
//			}
//			for (TraceNode node : entry.getConsumers()) {
//				int idx = 1;
//				ps.setString(idx++, entry.getVarID());
//				ps.setInt(idx++, traceId);
//				ps.setInt(idx++, node.getOrder());
//				ps.setInt(idx++, READ);
//				ps.addBatch();
//			}
//			if (++count >= BATCH_SIZE) {
//				ps.executeBatch();
//				count = 0;
//			}
//		}
//		if (count > 0) {
//			ps.executeBatch();
//		}
//	}

	private void setNodeOrder(PreparedStatement ps, int idx, TraceNode node) throws SQLException {
		if (node != null) {
			ps.setInt(idx, node.getOrder());
		} else {
			ps.setNull(idx, java.sql.Types.INTEGER);
		}
	}
	
	private Map<TraceNode, Integer> insertLocation(int traceId, List<TraceNode> nodes, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String sql = "INSERT INTO Location (trace_id, class_name, line_number, is_conditional, is_return) "
				+ "VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		closables.add(ps);
		int count = 0;
		List<Integer> ids = new ArrayList<>();
		for (TraceNode node : nodes) {
			BreakPoint location = node.getBreakPoint();
			int idx = 1;
			ps.setInt(idx++, traceId);
			ps.setString(idx++, location.getDeclaringCompilationUnitName());
			ps.setInt(idx++, location.getLineNumber());
			ps.setBoolean(idx++, location.isConditional());
			ps.setBoolean(idx++, location.isReturnStatement());
			ps.addBatch();
			if (++count == BATCH_SIZE) {
				ps.executeBatch();
				ids.addAll(getGeneratedIntIds(ps));
				count = 0;
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}
		ids.addAll(getGeneratedIntIds(ps));
		if (ids.size() != nodes.size()) {
			throw new SQLException("Number of locations is incorrect!");
		}
		Map<TraceNode, Integer> result = new HashMap<>();
		for (int i = 0; i < nodes.size(); i++) {
			Integer locId = ids.get(i);
			result.put(nodes.get(i), locId);
		}
		insertControlScope(traceId, result, conn, closables);
		insertLoopScope(traceId, result, conn, closables);
		return result;
	}
	
	private void insertControlScope(int traceId, Map<TraceNode, Integer> locationIdMap, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String sql = "INSERT INTO ControlScope (trace_id, location_id, class_name, line_number, is_loop) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		int count = 0;
		for (Entry<TraceNode, Integer> entry : locationIdMap.entrySet()) {
			ControlScope controlScope = entry.getKey().getBreakPoint().getControlScope();
			if (controlScope != null && !controlScope.getRangeList().isEmpty()) {
				int locationId = entry.getValue();
				for (ClassLocation controlLoc : controlScope.getRangeList()) {
					int idx = 1;
					ps.setInt(idx++, traceId);
					ps.setInt(idx++, locationId);
					ps.setString(idx++, controlLoc.getClassCanonicalName());
					ps.setInt(idx++, controlLoc.getLineNumber());
					ps.setBoolean(idx++, controlScope.isLoop());
					ps.addBatch();
					if (++count == BATCH_SIZE) {
						ps.executeBatch();
						count = 0;
					}
				}
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}
	}
	
	private void insertLoopScope(int traceId, Map<TraceNode, Integer> locationIdMap, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String sql = "INSERT INTO LoopScope (trace_id, location_id, class_name, start_line, end_line) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		
		int count = 0;
		for (Entry<TraceNode, Integer> entry : locationIdMap.entrySet()) {
			SourceScope loopScope = entry.getKey().getBreakPoint().getLoopScope();
			if (loopScope != null) {
				int locationId = entry.getValue();
				int idx = 1;
				ps.setInt(idx++, traceId);
				ps.setInt(idx++, locationId);
				ps.setString(idx++, loopScope.getClassName());
				ps.setInt(idx++, loopScope.getStartLine());
				ps.setInt(idx++, loopScope.getEndLine());
				ps.addBatch();
				if (++count == BATCH_SIZE) {
					ps.executeBatch();
					count = 0;
				}
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}
	}

	
}
