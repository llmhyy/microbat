package microbat.probability.SPP.vectorization.vector;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class NodeVector extends Vector {
	
	public static final int NUM_WRITTEN_VARS = 10;
	public static final int NUM_READ_VARS = 10;
	public static final int NUM_FUNC = 5;
	public static final int DIMENSION = OperationVector.DIMENSION +
										VariableVector.DIMENSION * NodeVector.NUM_READ_VARS +
										VariableVector.DIMENSION * NodeVector.NUM_WRITTEN_VARS +
										FunctionVector.DIMENSION * NodeVector.NUM_FUNC +
										EnvironmentVector.DIMENSION;
	
	private final OperationVector optVector;
	private final VariableVector[] readVarVectors;
	private final VariableVector[] writtenVarVectors;
	private final EnvironmentVector envVector;
	private final FunctionVector[] funcVectors;
	
	public NodeVector() {
		this.optVector = new OperationVector();
		this.readVarVectors = new VariableVector[NodeVector.NUM_READ_VARS];
		for (int idx=0; idx<this.readVarVectors.length; idx++) {
			this.readVarVectors[idx] = new VariableVector();
		}
		this.writtenVarVectors = new VariableVector[NodeVector.NUM_WRITTEN_VARS];
		for (int idx=0; idx<this.readVarVectors.length; idx++) {
			this.writtenVarVectors[idx] = new VariableVector();
		}
		this.envVector = new EnvironmentVector();
		this.funcVectors = new FunctionVector[NodeVector.NUM_FUNC];
		for (int idx=0; idx<this.funcVectors.length; idx++) {
			this.funcVectors[idx] = new FunctionVector();
		}
		
		this.vector = ArrayUtils.addAll(this.vector, this.optVector.getVector());
		for (VariableVector readVarVector : this.readVarVectors) {
			this.vector = ArrayUtils.addAll(this.vector, readVarVector.getVector());
		}
		for (VariableVector writtenVarVector : this.writtenVarVectors) {
			this.vector = ArrayUtils.addAll(this.vector, writtenVarVector.getVector());
		}
		this.vector = ArrayUtils.addAll(this.vector, this.envVector.getVector());
		for (FunctionVector funcVector : this.funcVectors) {
			this.vector = ArrayUtils.addAll(this.vector, funcVector.getVector());
		}
	}
	
	public NodeVector(final TraceNode node) {
		
		// Operation vector
		this.optVector = new OperationVector(node);
		
		// Read variables vector
		this.readVarVectors = new VariableVector[NodeVector.NUM_READ_VARS];
		List<VarValue> readVars = node.getReadVariables();
		readVars.removeIf(var -> var.isThisVariable());
		for (int idx=0; idx<this.readVarVectors.length; idx++) {
			if (idx<readVars.size()) {
				this.readVarVectors[idx] = new VariableVector(readVars.get(idx));
			} else {
				this.readVarVectors[idx] = new VariableVector();
			}
		}
		
		// Written variables vector
		this.writtenVarVectors = new VariableVector[NodeVector.NUM_WRITTEN_VARS];
		List<VarValue> writtenVars = node.getWrittenVariables();
		writtenVars.removeIf(var -> var.isThisVariable());
		for (int idx=0; idx<this.readVarVectors.length; idx++) {
			if (idx<writtenVars.size()) {
				this.writtenVarVectors[idx] = new VariableVector(writtenVars.get(idx));
			} else {
				this.writtenVarVectors[idx] = new VariableVector();
			}
		}
		
		// Environment vector
		this.envVector = new EnvironmentVector(node);
		
		// Function vector
		this.funcVectors = FunctionVector.constructFuncVectors(node, NodeVector.NUM_FUNC);
		
		this.vector = ArrayUtils.addAll(this.vector, this.optVector.getVector());
		for (VariableVector readVarVector : this.readVarVectors) {
			this.vector = ArrayUtils.addAll(this.vector, readVarVector.getVector());
		}
		for (VariableVector writtenVarVector : this.writtenVarVectors) {
			this.vector = ArrayUtils.addAll(this.vector, writtenVarVector.getVector());
		}
		this.vector = ArrayUtils.addAll(this.vector, this.envVector.getVector());
		for (FunctionVector funcVector : this.funcVectors) {
			this.vector = ArrayUtils.addAll(this.vector, funcVector.getVector());
		}
	}
	
	public OperationVector getOptVector() {
		return this.optVector;
	}
	
	public VariableVector[] getReadVarVectors() {
		return this.readVarVectors;
	}
	
	public VariableVector[] getWrittenVectors() {
		return this.writtenVarVectors;
	}
	
	public EnvironmentVector getEnvVector() {
		return this.envVector;
	}
	
	public FunctionVector[] getFuncVectors() {
		return this.funcVectors;
	}
}
