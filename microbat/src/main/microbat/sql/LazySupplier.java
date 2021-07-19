package microbat.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import microbat.handler.xml.VarValueXmlReader;
import microbat.model.BreakPoint;
import microbat.model.trace.LazyTraceNode;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.Pair;

public class LazySupplier {
	private final String traceId;
	private final String RW_QUERY = "SELECT read_vars, written_vars from Step where step_order = ? AND trace_id = ?";
	private final String READ_KEY = "read_vars";
	private final String WRITE_KEY = "written_vars";
	private final String INVOCATION_CHILDREN_QUERY = "SELECT * from Step where invocation_parent = ? AND trace_id = ?";
	private final String LOOP_CHILDREN_QUERY = "SELECT * from Step where loop_parent = ? AND trace_id = ?";
	private final String LOCATION_ID_QUERY = "SELECT * FROM Location where location_id = ? AND trace_id = ?";
	private Connection conn;
	private List<AutoCloseable> closables = new ArrayList<>();

	public LazySupplier(String traceId) {
		this.traceId = traceId;
	}

	public Pair<List<VarValue>, List<VarValue>> loadRWVars(TraceNode step) {
		this.closables = new ArrayList<>();
		try {
			this.conn = DbService.getConnection();
			PreparedStatement ps = this.conn.prepareStatement(RW_QUERY);
			ps.setInt(1, step.getOrder());
			ps.setString(2, traceId);
			ResultSet rs = ps.executeQuery();
			closables.add(rs);
			closables.add(ps);
			// read_vars
			List<VarValue> readVars = DbService.toVarValue(rs.getString(READ_KEY));
			// written_vars
			List<VarValue> writeVars = DbService.toVarValue(rs.getString(WRITE_KEY));
			return Pair.of(readVars, writeVars);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbService.closeDb(this.conn, this.closables);
			this.conn = null;
			this.closables = new ArrayList<>();
		}
		return null;
	}

	public List<TraceNode> loadInvocationChildren(TraceNode parent) {
		return this.loadAbstractChildren(parent, INVOCATION_CHILDREN_QUERY);
	}

	public List<TraceNode> loadLoopChildren(TraceNode parent) {
		return this.loadAbstractChildren(parent, LOOP_CHILDREN_QUERY);
	}

	private List<TraceNode> loadAbstractChildren(TraceNode parent, String query) {
		try {
			this.conn = DbService.getConnection();
			PreparedStatement ps = this.conn.prepareStatement(query);
			ps.setInt(1, parent.getOrder());
			ps.setString(2, traceId);
			ResultSet rs = ps.executeQuery();
			closables.add(rs);
			closables.add(ps);

			List<TraceNode> res = new ArrayList<>();
			while (rs.next()) {
				int childNodeOrder = rs.getInt("step_order");
				TraceNode traceNode = new LazyTraceNode(null, null, childNodeOrder, parent.getTrace(), this);
				String locationId = rs.getString("location_id");
				BreakPoint breakPoint = this.loadBreakPoint(locationId, parent.getTrace().getId());
				traceNode.setBreakPoint(breakPoint);
				res.add(traceNode);
			}
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbService.closeDb(this.conn, this.closables);
			this.conn = null;
			this.closables = new ArrayList<>();
		}
		return null;
	}
	
	/*
	 * loadBreakPoint does not close or open connections
	 * only call this method in getAbstractChildren
	 */
	private BreakPoint loadBreakPoint(String locationId, String traceId) {
		BreakPoint breakPoint = null;
		try {
			PreparedStatement ps = this.conn.prepareStatement(LOCATION_ID_QUERY);
			ps.setString(1, locationId);
			ps.setString(2, traceId);
			ResultSet rs = ps.executeQuery();
			closables.add(rs);
			closables.add(ps);

			while (rs.next()) {
				String className = rs.getString("class_name");
				int lineNo = rs.getInt("line_number");
				breakPoint = new BreakPoint(className, className, lineNo);
				breakPoint.setConditional(rs.getBoolean("is_conditional"));
				breakPoint.setReturnStatement(rs.getBoolean("is_return"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return breakPoint;
	}
}
