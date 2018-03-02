package microbat.agent;

import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;

public class ExecTraceFileReader {
	private String msg;
	private Trace trace;
	
	public Trace read(String execTraceFile) {
		RunningInfo info = RunningInfo.readFromFile(execTraceFile);
		this.trace = info.getTrace();
		this.msg = info.getProgramMsg();
		return trace;
	}
	
	public String getMsg() {
		return msg;
	}
}
