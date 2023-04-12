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
	
	private final OperationVector optVector;
	private final VariableVector[] refVarVectors;
	private final VariableVector targetVarVector;
	private final EnvironmentVector envVector;
	private final FunctionVector funcVector;
	 
	
}
