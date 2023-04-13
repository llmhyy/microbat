package microbat.probability.SPP.vectorization.vector;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Vectorization of Trace Node
 * @author David
 */
public class NodeVector extends Vector {
	
	public static final int NUM_REF_VARS = 10;
	public static final int NUM_FUNCS = 10;
	
	private final OperationVector optVector;
	private final VariableVector[] refVarVectors;
	private final VariableVector targetVarVector;
	private final EnvironmentVector envVector;
	private final FunctionVector[] funcVectors;
	 
	public NodeVector(final TraceNode node, final VarValue targetVar, final boolean backward) {
		this.optVector = new OperationVector(node);
		this.refVarVectors = VariableVector.constructVarVectors(
			backward ? node.getWrittenVariables() : node.getReadVariables(),
			NodeVector.NUM_FUNCS);
		this.targetVarVector = new VariableVector(targetVar);
		this.envVector = new EnvironmentVector(node);
		this.funcVectors = FunctionVector.constructFuncVectors(node, NodeVector.NUM_FUNCS);
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.optVector);
		strBuilder.append(",");
		for (VariableVector refVarVector : this.refVarVectors) {
			strBuilder.append(refVarVector);
			strBuilder.append(",");
		}
		strBuilder.append(this.targetVarVector);
		strBuilder.append(",");
		strBuilder.append(this.envVector);
		strBuilder.append(",");
		for (FunctionVector funcVector : this.funcVectors) {
			strBuilder.append(funcVector);
			strBuilder.append(",");
		}
		strBuilder.deleteCharAt(strBuilder.length()-1);
		return strBuilder.toString();
	}
}
