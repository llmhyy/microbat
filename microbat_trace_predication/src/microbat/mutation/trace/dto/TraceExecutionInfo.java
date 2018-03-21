package microbat.mutation.trace.dto;

import microbat.model.trace.Trace;

public class TraceExecutionInfo {
	private Trace trace;
	private String execPath;

	public TraceExecutionInfo(Trace trace, String execPath) {
		this.trace = trace;
		this.execPath = execPath;
	}

	public Trace getTrace() {
		return trace;
	}

	public String getExecPath() {
		return execPath;
	}

}
