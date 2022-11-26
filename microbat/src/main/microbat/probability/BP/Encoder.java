package microbat.probability.BP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.BP.constraint.BitRepresentation;
import microbat.probability.BP.constraint.Constraint;
import microbat.probability.BP.constraint.PriorConstraint;

/**
 * Encoder is the abstract class of Probability Encoder with different purpose.
 * 
 * They are used to calculate the probability of correctness
 * of variables and statements.
 * 
 * @author David
 *
 */
public abstract class Encoder {
	
	/**
	 * Complete trace of target program
	 */
	protected Trace trace;
	
	/**
	 * Execution list after dynamic slicing the target trace
	 */
	protected List<TraceNode> executionList;
	
	/**
	 * Mapping between variable id and variable reference
	 */
	protected Map<String, List<VarValue>> varIDMap = new HashMap<>();
	
	/**
	 * Constructor
	 * @param trace	Complete trace of target program
	 * @param executionList Execution list after dynamic slicing the target trace
	 */
	public Encoder(Trace trace, List<TraceNode> executionList) {
		this.trace = trace;
		this.executionList = executionList;
	}
	
	/**
	 * Perform probability encoding
	 */
	abstract public void encode();
	
	/**
	 * We will not consider the target node with more than 30 predicates
	 * because it will be too expensive too calculate
	 * 
	 * We will not consider the target node with no predicates.
	 * 
	 * @param node Target node
	 * @return True if the node will not be considered. False otherwise.
	 */
	protected boolean isSkippable(TraceNode node) {
		return Constraint.countPreds(node) >= 30 || Constraint.countPreds(node) == 0;
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
	
	/**
	 * Get all the variable that match the given ID
	 * @param varID ID of target variable
	 * @return List of variable that match the ID
	 */
	protected List<VarValue> getVarByID(final String varID) {
		if (this.varIDMap.containsKey(varID)) {
			return this.varIDMap.get(varID);
		} else {
			return this.getVarByID_default(varID);
		}
	}	
	
	// Helper function for getting variable by id
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
}
