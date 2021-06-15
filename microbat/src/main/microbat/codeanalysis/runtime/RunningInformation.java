package microbat.codeanalysis.runtime;

import java.util.List;

import microbat.model.trace.Trace;

public class RunningInformation {
	private String programMsg;
	private int expectedSteps;
	private int collectedSteps;
	private List<Trace> traceList;
	private Trace mainTrace;
	
	public RunningInformation(String programMsg, int expectedSteps, int collectedSteps, List<Trace> traceList) {
		this.programMsg = programMsg;
		this.expectedSteps = expectedSteps;
		this.collectedSteps = collectedSteps;
		this.traceList = traceList;
		
		if (!this.traceList.isEmpty()) {
			this.mainTrace = this.traceList.get(0);
		}
		
		for(Trace trace: traceList) {
			if(trace.isMain()) {
				this.mainTrace = trace;
				break;
			}
		}
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
		return traceList;
	}

	public void setTraceList(List<Trace> traceList) {
		this.traceList = traceList;
	}

	public Trace getMainTrace() {
		return mainTrace;
	}

	public void setMainTrace(Trace mainTrace) {
		this.mainTrace = mainTrace;
	}

}
