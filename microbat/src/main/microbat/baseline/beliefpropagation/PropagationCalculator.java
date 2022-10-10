package microbat.baseline.beliefpropagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.baseline.bytecode.ByteCode;
import microbat.baseline.bytecode.ByteCodeList;
import microbat.baseline.constraints.PropagationProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Propagation calculator is used to calculate the propagation probability of different statement.
 * It is used when baseline is creating A2 constraint
 * @author David
 *
 */
public class PropagationCalculator {
	
//	private List<VarValue> usedArraies;
//	private Map<String, Integer> classCount;
	
	private List<String> primitiveTypes;
	
	public PropagationCalculator() {
		this.primitiveTypes = new ArrayList<>();
		primitiveTypes.add("byte");
		primitiveTypes.add("short");
		primitiveTypes.add("int");
		primitiveTypes.add("long");
		primitiveTypes.add("float");
		primitiveTypes.add("double");
		primitiveTypes.add("char");
		primitiveTypes.add("boolean");
	}
	
	/**
	 * Calculate the propagation probability based on the statement type and target variable
	 * @param node Target trace node
	 * @param var Target variable
	 * @return Propagation probability
	 */
	public double calPropProb(TraceNode node, VarValue var) {
		
		ByteCodeList executedByteCode = new ByteCodeList(node.getBytecode());
		
		// For one to one statement, directly return High regardless of target variable
		if (executedByteCode.isOneToOne()) {
			return PropagationProbability.HIGH;
		}
		
		double prob = PropagationProbability.HIGH;
		
		/*
		 * By now, we don't have way to calculate the propagation probability
		 * for array access and the attribute access statement.
		 * 
		 * Here is how we handle array access by now, for the array variable and
		 * the accessing index, we assume it to be UNCERTAIN
		 * 
		 * Here is how we handle class attribute access by now. For the class
		 * variable, we assume it to be UNCERTAIN
		 */
		
		
		for (ByteCode byteCode : executedByteCode) {
			if (byteCode.isManyToOne()) {
//				System.out.println("byteCode is many to one");
				prob *= PropagationProbability.UNCERTAIN;
			} else if (byteCode.isArrayAccess() && this.isArray(var)) {
//				System.out.println("byteCode is array access");
				prob *= PropagationProbability.UNCERTAIN;
			} else if (byteCode.isAttrAccess() && this.isSelfDefinedClass(var)) {
//				System.out.println("byteCode is attribute access");
				prob *= PropagationProbability.UNCERTAIN;
			} 
			else if (byteCode.isFuncCall() && this.isSelfDefinedClass(var)) {
//				System.out.println("byteCode is function call");
				prob *= PropagationProbability.UNCERTAIN;
			}
		}
		
		return PropagationProbability.HIGH;
	}
	
	/**
	 * Check is the give variable an array
	 * 
	 * The type name of array contain the string "[]"
	 * @param var Variable to test
	 * @return True if the variable is an array. False otherwise
	 */
	private boolean isArray(VarValue var) {
		return var.getType().contains("[]");
	}
	
	/**
	 * Check is the given variable a self defined class
	 * 
	 * Self defined class is not primitive type and is not an array
	 * @param var Variable to test
	 * @return True if the variable is self defined class. False otherwise
	 */
	private boolean isSelfDefinedClass(VarValue var) {
		if (this.primitiveTypes.contains(var.getType())) {
			return false;
		}
		
		if (this.isArray(var)) {
			return false;
		}
		
		return true;
	}
}
