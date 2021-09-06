package microbat.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.Pair;

public class GraphTraceRetriever implements TraceRetriever {
	private final String runId;
	private static final String CONNECTION_URI = "jdbc:neo4j:bolt://localhost";

	public GraphTraceRetriever(String runId) {
		this.runId = runId;
	}

	public void printGreeting(final String message) {
		try (Connection conn = DriverManager.getConnection(CONNECTION_URI, "neo4j", "microbat")) {
			String query = "MATCH (t:Trace) RETURN t";
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						System.out.println(rs.getString("t"));
					}
				}
			}
		} catch (SQLException err) {
			System.out.println("Exception found");
		}
	}

	@Override
	public List<Trace> getTraces(String runId) {
		try (Connection conn = DriverManager.getConnection(CONNECTION_URI, "neo4j", "microbat")) {
			String query = "MATCH (t:Trace) RETURN t";
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				try (ResultSet rs = stmt.executeQuery()) {
					List<Trace> traces = new ArrayList<>();
					while (rs.next()) {
						System.out.println(rs.getString("t"));
						Trace trace = new Trace(rs.getString("_id"));
						String threadId = rs.getString("thread_id");
						String threadName = rs.getString("thread_name");
						boolean isMain = rs.getBoolean("isMain");
						trace.setThreadId(Long.parseLong(threadId));
						trace.setThreadName(threadName);
						trace.setMain(isMain);
					}
					return traces;
				}
			}
		} catch (SQLException err) {
			System.out.println("Exception found");
			return null;
		}
	}

	@Override
	public Pair<List<VarValue>, List<VarValue>> loadRWVars(TraceNode step, String traceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		try (Connection conn = DriverManager.getConnection(CONNECTION_URI, "neo4j", "microbat")) {
			String query = "MATCH (t:Trace) RETURN t";
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						System.out.println(rs.getString("t"));
					}
				}
			}
		} catch (SQLException err) {
			System.out.println("Exception found");
		}
	}
}
