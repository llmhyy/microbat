package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;


/**
 * Vectorization of Trace Node
 * @author David
 */
public class NodeVector extends Vector {
	
	public static final int NUM_REF_VARS = 10;
	public static final int NUM_FUNCS = 10;
	public static final int DIMENSION = OperationVector.DIMENSION + 
										VariableVector.DIMENSION * NodeVector.NUM_REF_VARS + 
										VariableVector.DIMENSION +
										EnvironmentVector.DIMENSION + 
										FunctionVector.DIMENSION * NodeVector.NUM_FUNCS;
	
	private final OperationVector optVector;
	private final VariableVector[] refVarVectors;
	private final VariableVector targetVarVector;
	private final EnvironmentVector envVector;
	private final FunctionVector[] funcVectors;
	
	public NodeVector() {
		
		this.optVector = new OperationVector();
		this.refVarVectors = new VariableVector[NodeVector.NUM_REF_VARS];
		for (int idx=0; idx<this.refVarVectors.length; idx++) {
			this.refVarVectors[idx] = new VariableVector();
		}
		this.targetVarVector = new VariableVector();
		this.envVector = new EnvironmentVector();
		this.funcVectors = new FunctionVector[NodeVector.NUM_FUNCS];
		for (int idx=0; idx<this.funcVectors.length; idx++) {
			this.funcVectors[idx] = new FunctionVector();
		}
		
		this.vector = ArrayUtils.addAll(this.vector, optVector.getVector());
		for (VariableVector refVarVector : this.refVarVectors) {
			this.vector =  ArrayUtils.addAll(this.vector, refVarVector.getVector());
		}
		this.vector = ArrayUtils.addAll(this.vector, this.targetVarVector.getVector());
		this.vector = ArrayUtils.addAll(this.vector, this.envVector.getVector());
		for (FunctionVector funcVector : this.funcVectors) {
			this.vector = ArrayUtils.addAll(this.vector, funcVector.getVector());
		}
		
		if (this.vector.length != NodeVector.DIMENSION) {
			throw new RuntimeException("dimension not match");
		}
	}
	
	/**
	 * If backward = true, source are written variables, target are read variable
	 * If backward = false, source are read variables, target are written variable
	 * We must have target variable
	 * @param node
	 * @param targetVar
	 * @param backward
	 */
	public NodeVector(final TraceNode node, final VarValue targetVar, final boolean backward) {
		this.optVector = new OperationVector(node);
		this.refVarVectors = VariableVector.constructVarVectors(
			backward ? node.getWrittenVariables() : node.getReadVariables(),
			NodeVector.NUM_REF_VARS);
		this.targetVarVector = new VariableVector(targetVar);
		this.envVector = new EnvironmentVector(node);
		this.funcVectors = FunctionVector.constructFuncVectors(node, NodeVector.NUM_FUNCS);
		
		this.vector = ArrayUtils.addAll(this.vector, optVector.getVector());
		for (VariableVector refVarVector : this.refVarVectors) {
			this.vector =  ArrayUtils.addAll(this.vector, refVarVector.getVector());
		}
		this.vector = ArrayUtils.addAll(this.vector, this.targetVarVector.getVector());
		this.vector = ArrayUtils.addAll(this.vector, this.envVector.getVector());
		for (FunctionVector funcVector : this.funcVectors) {
			this.vector = ArrayUtils.addAll(this.vector, funcVector.getVector());
		}
	}
	
	public OperationVector getOptVector() {
		return this.optVector;
	}
	
	public VariableVector[] getRefVarVectors() {
		return this.refVarVectors;
	}
	
	public VariableVector getTargetVarVector() {
		return this.targetVarVector;
	}
	
	public EnvironmentVector getEnvVector() {
		return this.envVector;
	}
	
	public FunctionVector[] getFuncVectors() {
		return this.funcVectors;
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
