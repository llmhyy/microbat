package microbat.mutation.trace.dto;

import microbat.codeanalysis.runtime.PreCheckInformation;
import microbat.model.trace.Trace;

public class TraceExecutionInfo {
	private Trace trace;
	private String execPath;
	private PreCheckInformation precheckInfo;

	public TraceExecutionInfo(PreCheckInformation precheckInfo, Trace trace, String execPath) {
		this.trace = trace;
		this.execPath = execPath;
		this.precheckInfo = precheckInfo;
	}

	public Trace getTrace() {
		return trace;
	}

	public String getExecPath() {
		return execPath;
	}

	public PreCheckInformation getPrecheckInfo() {
		return precheckInfo;
	}

}
