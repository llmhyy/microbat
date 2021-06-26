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
	FILE,SQLITE3,MYSQL;
	
	public static TraceRecorder create(AgentParams params) {
		switch (params.getTraceRecorderName()) {
		case "FILE":
			return new FileRecorder(params);
		case "SQLITE3":
			return new SqliteRecorder(params.getDumpFile(), params.getRunId());
//		case "MYSQL":
//			return new MysqlRecorder(params.getRunId());
		default:
			return new SqliteRecorder(params.getDumpFile(), params.getRunId());
		}
	}

}
