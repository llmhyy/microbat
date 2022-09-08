package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.baseline.BitRepresentation;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public abstract class Encoder {
	protected Trace trace;
	protected List<TraceNode> executionList;
	
	protected Map<String, List<VarValue>> varIDMap;
	
	public Encoder(Trace trace, List<TraceNode> executionList) {
		this.trace = trace;
		this.executionList = executionList;
		
		this.varIDMap = new HashMap<>();
	}
	
	abstract public void encode();
	
//	protected List<VarValue> getVarByID(final String varID) {
//		List<VarValue> vars = new ArrayList<>();
//		for (TraceNode node : this.executionList) {
//			for (VarValue readVar : node.getReadVariables()) {
//				if (readVar.getVarID().equals(varID)) {
//					vars.add(readVar);
//				}
//			}
//			for (VarValue writeVar : node.getWrittenVariables()) {
//				if (writeVar.getVarID().equals(varID)) {
//					vars.add(writeVar);
//				}
//			}
//			
//		}
//		return vars;
//	}
	
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
	
	/**
	 * Determine should we skip the current node when generating constraints
	 * Condition:
	 * 1) We will not consider the node if the number of predicate is smaller than 2,
	 *    because no constraint can be generated in this case.
	 * 2) We will not consider the node if the number of predicate is larger than 30,
	 *    because it is too expensive to calculate.
	 * 3) We will not consider the node if the node do not have control dominator while
	 * 	  there are no read or written variables, because reasonable constraint can be
	 * 	  formed.
	 * @param node Target node
	 * @return True if node can be skipped. False otherwise
	 */
	protected boolean isSkippable(TraceNode node) {
		return this.countPredicates(node) <= 1 || this.countPredicates(node) > 30 ||
			   (this.countReadVars(node) == 0 && !this.hasControlDom(node) ||
			   (this.countWriteVars(node) == 0 && !this.hasControlDom(node)));
//		return this.countPredicates(node) > 30;
	}
	
	protected boolean hasControlDom(TraceNode node) {
		return node.getControlDominator() != null;
	}
	
	/**
	 * Get the branch decision variable in the given trace node
	 * @param controlDom Control Dominator Node
	 * @return Branch dicision variable
	 */
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
	
	/**
	 * Build a mapping between variable ID and variable
	 * 
	 * This method assume that the duplicated variable ID
	 * problem is fixed.
	 */
	protected void construntVarIDMap() {
		for (TraceNode node : this.executionList) {
			for (VarValue readVar : node.getReadVariables()) {
				this.addVarIDPair(readVar.getVarID(), readVar);
			}
			
			for (VarValue writeVar : node.getWrittenVariables()) {
				this.addVarIDPair(writeVar.getVarID(), writeVar);
			}
		}
	}
	
	// Helper function to construct the var id map.
	private void addVarIDPair(String varID, VarValue var) {
		if (this.varIDMap.containsKey(varID)) {
			this.varIDMap.get(varID).add(var);
		} else {
			List<VarValue> vars = new ArrayList<>();
			vars.add(var);
			this.varIDMap.put(varID, vars);
		}
	}
	
	protected List<VarValue> getVarByID_default(final String varID) {
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
	
	/**
	 * Get all the variable that match the given ID
	 * @param varID ID of target variable
	 * @return List of variable that match the ID
	 */
	protected List<VarValue> getVarByID(final String varID) {
		return this.varIDMap.getOrDefault(varID, this.getVarByID_default(varID));
	}
}
