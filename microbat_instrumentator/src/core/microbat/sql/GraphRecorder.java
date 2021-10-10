package microbat.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.neo4j.jdbc.Neo4jDriver;
import org.neo4j.jdbc.bolt.impl.BoltNeo4jDriverImpl;

import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class GraphRecorder {
	private String runId;
	// TODO: ensure query interpolation format
	private static final String CONNECTION_URI = "jdbc:neo4j:bolt://localhost";
	private static final String CREATE_STEP_IN_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.trace_id = b.trace_id AND a.step_in= b.step_order CREATE (a)-[:STEP_IN]->(b)";
	private static final String CREATE_CONTROL_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.trace_id = b.trace_id AND a.step_order = b.control_dominator CREATE (a)-[:CONTROL_DOMINATES]->(b)";
	private static final String CREATE_INVOCATION_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.trace_id = b.trace_id AND a.step_order = b.invocation_parent CREATE (a)-[:INVOKES]->(b)";
	private static final String CREATE_LOOP_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.trace_id = b.trace_id AND a.step_order = b.loop_parent CREATE (a)-[:LOOPS]->(b)";
	private static final String CREATE_LOCATION_RELATIONS = "MATCH (a: Step), (b: Location) WHERE a.trace_id = b.trace_id AND a.location_id = b.location_id CREATE (a)-[:AT]->(b)";
	private static final String INSERT_STEPS_QUERY = "MERGE (a:Step {trace_id: {1}, step_order: {2}, step_in: {3}, step_over:{4}, control_dominator: {5}, invocation_parent: {6}, loop_parent: {7}, location_id: {8}, time: {9}})";
	private static final String INSERT_LOCATION = "MERGE (a:Location {location_id: {1}, trace_id: {2}, class_name: {3}, line_number:{4}, is_conditional:{5}, is_return:{6}})";

	public GraphRecorder(String runId) {
		this.runId = runId;
		try {
			DriverManager.registerDriver(new org.neo4j.jdbc.Driver());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void store(List<Trace> traces) {
		try (Connection conn = DriverManager.getConnection(CONNECTION_URI, "neo4j", "microbat")) {
			// Store this run
			String createRunQuery = "CREATE (r: RUN {runId: {1}})";
			try (PreparedStatement stmt = conn.prepareStatement(createRunQuery)) {
				stmt.setString(1, this.runId);
				stmt.execute();
			}
			for (Trace trace : traces) {
				String traceId = UUID.randomUUID().toString();
				this.insertSteps(conn, trace, traceId);
			}
			this.createRelations(conn, CREATE_STEP_IN_RELATIONS);
			this.createRelations(conn, CREATE_CONTROL_RELATIONS);
			this.createRelations(conn, CREATE_INVOCATION_RELATIONS);
			this.createRelations(conn, CREATE_LOOP_RELATIONS);
			this.createRelations(conn, CREATE_LOCATION_RELATIONS);
		} catch (SQLException err) {
			for (Enumeration<Driver> drivers = DriverManager.getDrivers(); drivers.hasMoreElements();)
				System.out.println(drivers.nextElement());
			err.printStackTrace();
		}
	}

	private void insertSteps(Connection conn, Trace trace, String traceId) {
		try (PreparedStatement stmt = conn.prepareStatement(INSERT_STEPS_QUERY)) {
			Set<BreakPoint> set = new HashSet<>();
			for (TraceNode node : trace.getExecutionList()) {
				int idx = 1;
				stmt.setString(idx++, traceId);
				stmt.setInt(idx++, node.getOrder());
				stmt.setInt(idx++, node.getStepOverNext().getOrder());
				stmt.setInt(idx++, node.getStepInNext().getOrder());
				stmt.setInt(idx++, node.getControlDominator().getOrder());
				stmt.setInt(idx++, node.getInvocationParent().getOrder());
				stmt.setInt(idx++, node.getLoopParent().getOrder());
				stmt.setString(idx++, node.getDeclaringCompilationUnitName() + "_" + node.getLineNumber());
				// TODO: create similar query for variables
//				ps.setString(idx++, generateXmlContent(node.getReadVariables()));
//				ps.setString(idx++, generateXmlContent(node.getWrittenVariables()));
				stmt.setDate(idx, new Date(node.getTimestamp()));
				stmt.addBatch();
				set.add(node.getBreakPoint());
			}
			insertLocations(conn, traceId, set);
			stmt.executeBatch();
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}

	private void insertLocations(Connection conn, String traceId, Set<BreakPoint> set) {
		try (PreparedStatement ps = conn.prepareStatement(INSERT_LOCATION)) {
			for (BreakPoint location : set) {
				int idx = 1;
				ps.setString(idx++, location.getDeclaringCompilationUnitName() + "_" + location.getLineNumber());
				ps.setString(idx++, traceId);
				ps.setString(idx++, location.getDeclaringCompilationUnitName());
				ps.setInt(idx++, location.getLineNumber());
				ps.setBoolean(idx++, location.isConditional());
				ps.setBoolean(idx++, location.isReturnStatement());
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}

	private void createRelations(Connection conn, String query) {
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.executeUpdate();
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}
}
