/**
 * 
 */
package microbat.sql;

import java.sql.Connection;
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
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.utils.StringUtils;

/**
 * @author knightsong SQL lite
 */
public class TraceRetriever01 extends SqliteServer {
	/**
	 * return Object[]: regression_id, buggy_trace id, correct_trace id
	 */
	public TraceRetriever01(String dbPath) {
		super(dbPath);
	}
	public String getLatestTrace(String projectName) throws SQLException {
		String result = null;
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<>();
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(
					"SELECT trace_id FROM Trace order by generated_time desc");
//			ps.setString(1, projectName);
			ResultSet rs = ps.executeQuery();
			closables.add(ps);
			closables.add(rs);

			if (rs.next()) {
				result = rs.getString("trace_id"); // regression_id
			} else {
				throw new SQLException(
						String.format("No record of Regression found for project %s", projectName));
			}
			
		} finally {
			closeDb(conn, closables);
		}
		
		return result;
	}
	
	public Trace retrieveTrace(String traceId) throws SQLException{
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<>();
		try {
			conn = getConnection();
			Trace trace = loadTrace(traceId, conn, closables);
			System.out.println("Retrieve done!");
			return trace;
		} finally {
			closeDb(conn, closables);
		}
	}
	
	protected Trace loadTrace(String traceId, Connection conn, List<AutoCloseable> closables) throws SQLException {
		Trace trace = new Trace(null); 
		// load step
		List<TraceNode> steps = loadSteps(traceId, conn, closables, trace);
		trace.setExectionList(steps);
		// load stepVar
		List<Object[]> rows = loadStepVariableRelation(traceId, conn, closables);
		Map<String, StepVariableRelationEntry> stepVariableTable = trace.getStepVariableTable();
		for (Object[] row : rows) {
			int stepOrder = (int) row[0];
			String varId = (String) row[1];
			int rw = (int) row[2];
			StepVariableRelationEntry entry = stepVariableTable.get(varId);
			if (entry == null) {
				entry = new StepVariableRelationEntry(varId);
				stepVariableTable.put(varId, entry);
			}
			if (rw == TraceRecorder.WRITE) {
				entry.addProducer(steps.get(stepOrder - 1));
			} else if (rw == TraceRecorder.READ) {
				entry.addConsumer(steps.get(stepOrder - 1));
			} else {
				throw new SQLException("Tabel StepVariableRelationEntry: Invalid RW value!");
			}
		}
		return trace;
	}
	
	/**
	 * return list of relation info [step_order, var_id, RW]
	 */
	private List<Object[]> loadStepVariableRelation(String traceId, Connection conn, List<AutoCloseable> closables) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT r.step_order, r.var_id, r.RW FROM StepVariableRelation r WHERE r.trace_id=?");
		ps.setString(1, traceId);
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		List<Object[]> result = new ArrayList<>();
		while (rs.next()) {
			int idx = 1;
			Object[] row = new Object[]{
					rs.getInt(idx++),
					rs.getString(idx++),
					rs.getInt(idx++)
			};
			result.add(row);
		}
		ps.close();
		rs.close();
		return result;
	}

	private List<TraceNode> loadSteps(String traceId, Connection conn, List<AutoCloseable> closables, Trace trace) 
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT s.* FROM Step s WHERE s.trace_id=?");
		ps.setString(1, traceId);
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);		
		int total = countNumberOfStep(traceId,conn,closables);
		long a =System.currentTimeMillis();
		List<TraceNode> allSteps = new ArrayList<>(total);
		for (int i = 0; i < total; i++) {
			allSteps.add(new TraceNode(null, null, i + 1, trace));
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
			String locID=rs.getString("location_id");
			List<TraceNode> list=null;
			if (locationIdMap.containsKey(locID)) {
				list=locationIdMap.get(locID);		
			}else {
				list=new ArrayList<>();			
			}
			list.add(step);
			locationIdMap.put(locID,list);

			String loadVarStep = "read_vars";
			try {
				// read_vars
				step.setReadVariables(toVarValue(rs.getString("read_vars")));
				// written_vars
				loadVarStep = "written_vars";
				step.setWrittenVariables(toVarValue(rs.getString("written_vars")));
			} catch (Exception e) {
				System.out.println(String.format("%s: Xml error at step: [trace_id, order] = [%d, %d]", loadVarStep, traceId, order));
				throw e;
			}
		}
		rs.close();
		ps.close();
		long b =System.currentTimeMillis();
		System.out.println("fill step "+(b-a));
		loadLocations(locationIdMap, conn, closables);		
		return allSteps;
	}
	
	/**
	 * Last_update knightsong Sep 9, 2020 
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private int countNumberOfStep(String traceId,Connection conn,List<AutoCloseable> closables) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT Count(*) FROM Step s WHERE s.trace_id=?");
		ps.setString(1, traceId);
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		return rs.getInt(1);
	}

	private void loadLocations(Map<String, List<TraceNode>> locIdStepMap, Connection conn, List<AutoCloseable> closables) throws SQLException {
		if (locIdStepMap.isEmpty()) {
			return ;
		}
		Set<String> locationSet = locIdStepMap.keySet();
		String matchList = StringUtils.joinWithApostrophe(locationSet, ",");
		/* control scope */
//		Map<Integer, ControlScope> controlScopeMap = loadControlScopes(locationSet, matchList, conn, closables);
		/* loop scope */
//		Map<Integer, SourceScope> loopScopeMap = loadLoopScope(locationSet, matchList, conn, closables);
		/* location */
		PreparedStatement ps = conn.prepareStatement(String.format(
				"SELECT location_id,class_name,line_number,is_conditional,is_return FROM Location WHERE location_id IN (%s)", 
					matchList));
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		while(rs.next()) {
			String locId = rs.getString("location_id");
			String className = rs.getString("class_name");
			int lineNo = rs.getInt("line_number");
			BreakPoint bkp = new BreakPoint(className, className, lineNo);
			bkp.setConditional(rs.getBoolean("is_conditional"));
			bkp.setReturnStatement(rs.getBoolean("is_return"));
			
			List<TraceNode>nodes=locIdStepMap.get(locId);
			for(TraceNode node :nodes) {
				node.setBreakPoint(bkp);
			}		
//			bkp.setControlScope(controlScopeMap.get(locId));
//			bkp.setLoopScope(loopScopeMap.get(locId));
		}
		ps.close();
	}
	protected List<VarValue> toVarValue(String xmlContent) {
//		xmlContent = xmlContent.replace("&#", "#");
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

	

