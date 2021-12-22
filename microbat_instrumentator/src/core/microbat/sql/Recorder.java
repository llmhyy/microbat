/**
 * 
 */
package microbat.sql;

import microbat.instrumentation.AgentParams;

/**
 * @author knightsong
 *
 */
public enum Recorder {
	FILE, SQLITE3, MYSQL, GRAPH;

	public static TraceRecorder create(AgentParams params) {
		switch (params.getTraceRecorderName()) {
		case "FILE":
			return new FileRecorder(params);
		case "SQLITE3":
		case "MYSQL":
			return new SqliteRecorder(params.getDumpFile(), params.getRunId());
		case "NEO4J":
			return new GraphRecorder(params.getRunId());
		default:
			return new FileRecorder(params);
		}
	}

}
