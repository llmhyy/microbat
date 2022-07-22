package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public abstract class Encoder {
	protected Trace trace;
	protected List<TraceNode> executionList;
	
	public Encoder(Trace trace, List<TraceNode> executionList) {
		this.trace = trace;
		this.executionList = executionList;
	}
	
	abstract public void encode();
	
	protected List<VarValue> getVarByID(final String varID) {
		List<VarValue> vars = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			for (VarValue readVar : node.getReadVariables()) {
				if (readVar.getVarID().equals(varID)) {
					vars.add(readVar);
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				if (writeVar.getVarID().equals(varID)) {
					vars.add(writeVar);
				}
			}
		}
		return vars;
	}
	
	protected int countReadVars(TraceNode node) {
		return node.getReadVariables().size();
	}
	
	protected int countWriteVars(TraceNode node) {
		return node.getWrittenVariables().size();
	}
	
	protected int countPredicates(TraceNode node) {
		int varCount = this.countReadVars(node) + this.countWriteVars(node);
		return node.getControlDominator() == null ? varCount : varCount + 1;
	}
	
	protected boolean isSkippable(TraceNode node) {
		return this.countReadVars(node) == 0 || this.countWriteVars(node) == 0;
	}
}
