package microbat.util;

import microbat.model.variable.Variable;

public class VariableUtil {

	/**
	 * For now, the virtual variable is never considered as equivalent (or, compatible). It means that the path between method invocation
	 * is not jumped, which facilitates the abstraction intention. That is, the jump occurs only in the same abstraction level.
	 * @param var1
	 * @param var2
	 * @return
	 */
	public static boolean isEquivalentVariable(Variable var1, Variable var2){
		String varID1 = var1.getVarID();
		String simpleVarID1 = Variable.truncateSimpleID(varID1);
		String varID2 = var2.getVarID();
		String simpleVarID2 = Variable.truncateSimpleID(varID2);
		
		boolean isEquivalentVariable = simpleVarID1.equals(simpleVarID2) || var1.getName().equals(var2.getName());
		
		return isEquivalentVariable;
	}

	public static String generateSimpleParentVariableID(String variableID){
		String simpleID = Variable.truncateSimpleID(variableID);
		String parentID = null;
		if(simpleID.contains(".")){
			parentID = simpleID.substring(0, simpleID.lastIndexOf("."));
		}
		
		if(simpleID.contains("[")){
			parentID = simpleID.substring(0, simpleID.lastIndexOf("["));
		}
		
		return parentID;
		
		
	}
}
