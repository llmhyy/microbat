package microbat.mutation.trace.dto;

import microbat.codeanalysis.runtime.PreCheckInformation;
import microbat.model.trace.Trace;

public class TraceExecutionInfo {
	public static final String PRECHECK_FILE_NAME = "precheck.info";
	private Trace trace;
	private String execPath;
	private String precheckInfoPath;
	private PreCheckInformation precheckInfo;

	public TraceExecutionInfo(PreCheckInformation precheckInfo, Trace trace, String execPath, String precheckPath) {
		this.trace = trace;
		this.execPath = execPath;
		this.precheckInfo = precheckInfo;
		this.precheckInfoPath = precheckPath;
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

	public String getPrecheckInfoPath() {
		return precheckInfoPath;
	}
	
	public void setTrace(Trace trace) {
		this.trace = trace;
	}
}
