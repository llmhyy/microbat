package microbat.instrumentation.trace.model;

import java.util.ArrayList;
import java.util.List;

import microbat.model.variable.Variable;

public class AccessVariableInfo {
	private int lineNo;
	private List<Integer> pcs;
	private List<Variable> readVars;
	private List<Variable> writtenVars;
	private Variable returnVar;
	
	public AccessVariableInfo(int lineNo) {
		this.lineNo = lineNo;
		pcs = new ArrayList<>(5);
	}
	
	public void addPc(int pc) {
		pcs.add(pc);
	}

	public void addReadVar(Variable var) {
		if (readVars == null) {
			readVars = new ArrayList<>(5);
		}
		readVars.add(var);
	}
	
	public void addWrittenVar(Variable var) {
		if (writtenVars == null) {
			writtenVars = new ArrayList<>();
		}
		writtenVars.add(var);
	}
	
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
	
	public Variable getReturnVar() {
		return returnVar;
	}

	public void setReturnVar(Variable returnVar) {
		this.returnVar = returnVar;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getInsertPc() {
		// TODO Auto-generated method stub
		return 0;
	}
}
