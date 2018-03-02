package microbat.codeanalysis.runtime;

import microbat.model.trace.Trace;

public class RunningInformation {
	private String programMsg;
	private int expectedSteps;
	private int collectedSteps;
	private Trace trace;
	
	public RunningInformation(String programMsg, int expectedSteps, int collectedSteps, Trace trace) {
		super();
		this.programMsg = programMsg;
		this.expectedSteps = expectedSteps;
		this.collectedSteps = collectedSteps;
		this.setTrace(trace);
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

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

}
