package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.Trace;

public class RunningInformation {
	private String programMsg;
	private int expectedSteps;
	private int collectedSteps;
	private TraceInfo traceInfo;
	
	public RunningInformation(String programMsg, int expectedSteps, int collectedSteps, TraceInfo traceInfo) {
		this.programMsg = programMsg;
		this.expectedSteps = expectedSteps;
		this.collectedSteps = collectedSteps;
		this.traceInfo = traceInfo;
	}
	
	public boolean isExpectedStepsMet(){
		double rate = 0.05;
		double min = expectedSteps * (1 - rate);
		double max = expectedSteps * (1 + rate);
		return min <= collectedSteps && collectedSteps <=max;
	}

	public String getProgramMsg() {
		return programMsg;
	}

	public void setProgramMsg(String programMsg) {
		this.programMsg = programMsg;
	}

	public int getExpectedSteps() {
		return expectedSteps;
	}

	public void setExpectedSteps(int expectedSteps) {
		this.expectedSteps = expectedSteps;
	}

	public int getCollectedSteps() {
		return collectedSteps;
	}

	public void setCollectedSteps(int collectedSteps) {
		this.collectedSteps = collectedSteps;
	}

	public List<Trace> getTraceList() {
		return this.traceInfo.getTraces().orElse(new ArrayList<>());
	}

	public Trace getMainTrace() {
		return this.traceInfo.getMainTrace().orElse(new Trace(""));
	}
}
