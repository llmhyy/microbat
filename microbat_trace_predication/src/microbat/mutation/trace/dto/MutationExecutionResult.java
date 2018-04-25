package microbat.mutation.trace.dto;

import microbat.model.trace.Trace;
import tregression.empiricalstudy.EmpiricalTrial;

public class MutationExecutionResult {
	public Trace correctTrace;
	public Trace bugTrace;
	private EmpiricalTrial trial;
	boolean isLoopEffective;
	boolean isValid;

	public Trace getCorrectTrace() {
		return correctTrace;
	}

	public Trace getBugTrace() {
		return bugTrace;
	}

	public boolean isLoopEffective() {
		return isLoopEffective;
	}

	public boolean isValid() {
		return isValid;
	}

	public EmpiricalTrial getTrial() {
		return trial;
	}

	public void setTrial(EmpiricalTrial trial) {
		this.trial = trial;
	}
	
	public void setCorrectTrace(Trace correctTrace) {
		this.correctTrace = correctTrace;
	}
	
	public void setBugTrace(Trace bugTrace) {
		this.bugTrace = bugTrace;
	}
	
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
}
