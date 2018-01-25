package microbat.instrumentation.trace.model;

import java.util.List;

import microbat.model.variable.Variable;

public class AccessVariableInfo {
	private List<Variable> readVars;
	private List<Variable> writtenVars;

	public List<Variable> getReadVars() {
		return readVars;
	}

	public void setReadVars(List<Variable> readVars) {
		this.readVars = readVars;
	}

	public List<Variable> getWrittenVars() {
		return writtenVars;
	}

	public void setWrittenVars(List<Variable> writtenVars) {
		this.writtenVars = writtenVars;
	}

}
