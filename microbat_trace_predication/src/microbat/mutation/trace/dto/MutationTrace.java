package microbat.mutation.trace.dto;

import microbat.model.trace.Trace;

public class MutationTrace {
	private TraceExecutionInfo trace;
	private boolean isTooLong;
	private boolean isKill;
	private boolean isTimeOut;

	public Trace getTrace() {
		return trace == null ? null : trace.getTrace();
	}
	
	public TraceExecutionInfo getTraceExecInfo() {
		return trace;
	}

	public void setTrace(TraceExecutionInfo traceExecutionInfo) {
		this.trace = traceExecutionInfo;
	}

	public boolean isTooLong() {
		return isTooLong;
	}

	public void setTooLong(boolean isTooLong) {
		this.isTooLong = isTooLong;
	}

	public boolean isKill() {
		return isKill;
	}

	public void setKill(boolean isKill) {
		this.isKill = isKill;
	}

	public boolean isTimeOut() {
		return isTimeOut;
	}

	public void setTimeOut(boolean isTimeOut) {
		this.isTimeOut = isTimeOut;
	}

	public String getTraceExecFile() {
		return trace.getExecPath();
	}

	public boolean isValid() {
		if (trace == null || trace.getTrace() == null || trace.getTrace().size() < 1) {
			return false;
		}
		return isKill && !isTooLong;
	}
}
