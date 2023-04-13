package microbat.probability.SPP.vectorization.vector;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Vectorization of Trace Node
 * @author David
 */
public class NodeVector extends Vector {
	
	private static final int NUM_REF_VARS = 10;
	private static final int NUM_FUNCS = 10;
	
	private final OperationVector optVector;
	private final VariableVector[] refVarVectors;
	private final VariableVector targetVarVector;
	private final EnvironmentVector envVector;
	private final FunctionVector[] funcVectors;
	 
	public NodeVector(final TraceNode node, final VarValue targetVar, final boolean backward) {
		this.optVector = new OperationVector(node);
		this.refVarVectors = VariableVector.constructVarVectors(
			backward ? node.getWrittenVariables() : node.getReadVariables(),
			NUM_FUNCS);
		this.targetVarVector = new VariableVector(targetVar);
		this.envVector = new EnvironmentVector(node);
		this.funcVectors = FunctionVector.constructFuncVectors(node, NodeVector.NUM_FUNCS);
	}
}
