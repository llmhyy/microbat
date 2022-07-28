package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
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
		return this.countPredicates(node) <= 1;
	}
	
	protected VarValue getControlDomValue(TraceNode controlDom) {
		for (VarValue writeVar : controlDom.getWrittenVariables()) {
			if (writeVar.getVarID().startsWith(ProbabilityEncoder.CONDITION_RESULT_ID_PRE)) {
				return writeVar;
			}
		}
		return null;
	}
	
	protected Constraint genPriorConstraint(VarValue var, double prob) {
		
		BitRepresentation varsIncluded = new BitRepresentation(1);
		varsIncluded.set(0);
		
		Constraint constraint = new PriorConstraint(varsIncluded, 0, prob);
		
		// When it is prior constraint, it doesn't matter if the id is read or write or control dominator variable
		// So let just add it to read variable id list
		constraint.addReadVarID(var.getVarID());
		
		return constraint;
	}
}
