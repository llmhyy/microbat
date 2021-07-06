/**
 * 
 */
package microbat.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.handler.xml.VarValueXmlReader;
import microbat.model.BreakPoint;
import microbat.model.trace.LazyTraceNode;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.Pair;
import sav.common.core.utils.StringUtils;

/**
 * The retriever works on any DBMS since DBMS-specific details are handled by DBService and connection factory
 * @author dingyuchen
 *
 */
public class TraceRetrieverImpl implements TraceRetriever {
	private static final String GET_LATEST_TRACE_ID_QUERY = 
			"SELECT trace_id, thread_id, thread_name, isMain FROM Trace WHERE run_id = ?";
	private static final String GET_TRACE_WITH_STEP = 
			"SELECT * from Trace INNER JOIN Step ON Trace.trace_id = Step.trace_id";
	private static final String GET_STEPS = 
			"SELECT s.* FROM Step s WHERE s.trace_id=?";
	private static final String GET_STEP_VARIABLE_RELATION = 
			"SELECT r.step_order, r.var_id, r.RW FROM StepVariableRelation r WHERE r.trace_id=?";
	private Connection conn;
	private List<AutoCloseable> closables = new ArrayList<>();

	public TraceRetrieverImpl() throws SQLException {
		this.conn = DbService.getConnection();
	}

	@Override
	public List<Trace> getTraces(String runId) {

		List<Trace> traces = new ArrayList<>();
		try {
			PreparedStatement ps = this.conn.prepareStatement(GET_LATEST_TRACE_ID_QUERY);
			ps.setString(1, runId);
			ResultSet rs = ps.executeQuery();
			this.closables.add(ps);
			this.closables.add(rs);

			while (rs.next()) {
				Trace trace = new Trace(rs.getString("trace_id"));
				String threadId = rs.getString("thread_id");
				String threadName = rs.getString("thread_name");
				boolean isMain = rs.getBoolean("isMain");
				trace.setThreadId(Long.parseLong(threadId));
				trace.setThreadName(threadName);
				trace.setMain(isMain);
				
				loadTrace(trace);

				traces.add(trace);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbService.closeDb(conn, closables);
			this.closables = new ArrayList<>();
		}

		return traces;
	}

	protected void loadTrace(Trace trace) throws SQLException {
		// load step
		List<TraceNode> steps = getSteps(trace);
		trace.setExecutionList(steps);
	}

	private List<TraceNode> getSteps(Trace trace) throws SQLException {
		String traceId = trace.getId();
		PreparedStatement ps = conn.prepareStatement(GET_STEPS);
		ps.setString(1, traceId);
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		int total = countNumberOfStep(traceId, conn, closables);
		long a = System.currentTimeMillis();
		List<TraceNode> allSteps = new ArrayList<>(total);
		for (int i = 0; i < total; i++) {
			TraceNode node = new LazyTraceNode(null, null, i + 1, trace, step -> this.loadRWVars(step, traceId));
			allSteps.add(node);
		}
		Map<String, List<TraceNode>> locationIdMap = new HashMap<>();
		while (rs.next()) {
			// step order
			int order = rs.getInt("step_order");
			if (order > total) {
				throw new SQLException("Detect invalid step order in result set!");
			}
			TraceNode step = allSteps.get(order - 1);

			step.setOrder(order);
			// control_dominator
			TraceNode controlDominator = getRelNode(allSteps, rs, "control_dominator");
			step.setControlDominator(controlDominator);
			if (controlDominator != null) {
				controlDominator.addControlDominatee(step);
			}
			// step_in
			TraceNode stepIn = getRelNode(allSteps, rs, "step_in");
			step.setStepInNext(stepIn);
			if (stepIn != null) {
				stepIn.setStepInPrevious(step);
			}
			// step_over
			TraceNode stepOver = getRelNode(allSteps, rs, "step_over");
			step.setStepOverNext(stepOver);
			if (stepOver != null) {
				stepOver.setStepOverPrevious(step);
			}
			// invocation_parent
			TraceNode invocationParent = getRelNode(allSteps, rs, "invocation_parent");
			step.setInvocationParent(invocationParent);
			if (invocationParent != null) {
				invocationParent.addInvocationChild(step);
			}
			// loop_parent
			TraceNode loopParent = getRelNode(allSteps, rs, "loop_parent");
			step.setLoopParent(loopParent);
			if (loopParent != null) {
				loopParent.addLoopChild(step);
			}

			// location_id
			String locID = rs.getString("location_id");
			List<TraceNode> list = null;
			if (locationIdMap.containsKey(locID)) {
				list = locationIdMap.get(locID);
			} else {
				list = new ArrayList<>();
			}
			list.add(step);
			locationIdMap.put(locID, list);
			
			// timestamp
			Date timestamp = rs.getDate("time");
			step.setTimestamp(timestamp.getTime());

		}
		long b = System.currentTimeMillis();
		System.out.println("fill step " + (b - a));
		loadLocations(locationIdMap, conn, closables);
		return allSteps;
	}
	
	public Pair<List<VarValue>, List<VarValue>> loadRWVars(TraceNode step, String traceId) {
		String loadVarStep = "read_vars";
		try {
			conn = DbService.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT read_vars, written_vars from Step where step_order = ? AND trace_id = ?");
			ps.setInt(1, step.getOrder());
			ps.setString(2, traceId);
			ResultSet rs = ps.executeQuery();
			closables.add(rs);
			closables.add(ps);
			// read_vars
			List<VarValue>readVars = toVarValue(rs.getString("read_vars"));
			// written_vars
			loadVarStep = "written_vars";
			List<VarValue>writeVars = toVarValue(rs.getString("written_vars"));
			return Pair.of(readVars, writeVars);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(String.format("%s: Xml error at step: [trace_id, order] = [%d, %d]", loadVarStep, traceId, step.getOrder()));
			throw e;
		} finally {
			DbService.closeDb(conn, closables);
			this.closables = new ArrayList<>();
		}
		return null;
	}

	/**
	 * Last_update knightsong Sep 9, 2020
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private int countNumberOfStep(String traceId, Connection conn, List<AutoCloseable> closables) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT Count(*) FROM Step s WHERE s.trace_id=?");
		ps.setString(1, traceId);
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		return rs.getInt(1);
	}

	private void loadLocations(Map<String, List<TraceNode>> locIdStepMap, Connection conn, List<AutoCloseable> closables)
			throws SQLException {
		if (locIdStepMap.isEmpty()) {
			return;
		}
		Set<String> locationSet = locIdStepMap.keySet();
		String matchList = StringUtils.joinWithApostrophe(locationSet, ",");
		/* control scope */
		// Map<Integer, ControlScope> controlScopeMap = loadControlScopes(locationSet,
		// matchList, conn, closables);
		/* loop scope */
		// Map<Integer, SourceScope> loopScopeMap = loadLoopScope(locationSet,
		// matchList, conn, closables);
		/* location */
		PreparedStatement ps = conn.prepareStatement(String.format(
				"SELECT location_id,class_name,line_number,is_conditional,is_return FROM Location WHERE location_id IN (%s)",
				matchList));
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		while (rs.next()) {
			String locId = rs.getString("location_id");
			String className = rs.getString("class_name");
			int lineNo = rs.getInt("line_number");
			BreakPoint bkp = new BreakPoint(className, className, lineNo);
			bkp.setConditional(rs.getBoolean("is_conditional"));
			bkp.setReturnStatement(rs.getBoolean("is_return"));

			List<TraceNode> nodes = locIdStepMap.get(locId);
			for (TraceNode node : nodes) {
				node.setBreakPoint(bkp);
			}
			// bkp.setControlScope(controlScopeMap.get(locId));
			// bkp.setLoopScope(loopScopeMap.get(locId));
		}
		ps.close();
	}

	protected List<VarValue> toVarValue(String xmlContent) {
		// xmlContent = xmlContent.replace("&#", "#");
		return VarValueXmlReader.read(xmlContent);
	}

	private TraceNode getRelNode(List<TraceNode> allSteps, ResultSet rs, String colName) throws SQLException {
		int relNodeOrder = rs.getInt(colName);
		if (!rs.wasNull()) {
			if (relNodeOrder > allSteps.size()) {
				System.err.println(String.format("index out of bound: size=%d, idx=%d", allSteps.size(), relNodeOrder));
				return null;
			}
			return allSteps.get(relNodeOrder - 1);
		}
		return null;
	}
}
