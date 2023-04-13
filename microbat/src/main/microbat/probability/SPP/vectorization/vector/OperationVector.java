package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.model.trace.TraceNode;

public class OperationVector extends Vector{
	
	public static final int DIMENSION = 256;
	
	private final boolean[] vector = new boolean[OperationVector.DIMENSION];
	
	public OperationVector(final TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		for (int idx=0; idx<this.vector.length; idx++) {
			this.vector[idx] = false;
		}
		for (ByteCode byteCode : byteCodeList) {
			short opCode = byteCode.getOpcode();
			this.vector[opCode] = true;
		}
	}
	
	public OperationVector(final OperationVector otherVector) {
		for (int idx=0; idx<this.vector.length; idx++) {
			this.vector[idx] = otherVector.vector[idx];
		}
	}
	
	public boolean[] getOperationArray() {
		return this.vector;
	}
	
	public boolean equals(Object otherObj) {
		if (otherObj instanceof OperationVector) {
			OperationVector otherVec = (OperationVector) otherObj;
			if (!Arrays.equals(this.vector, otherVec.vector)) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.vector[0] ? "1" : "0");
		for (int idx=1; idx<this.vector.length; ++idx) {
			strBuilder.append(",");
			strBuilder.append(this.vector[idx] ? "1" : "0");
		}
		return strBuilder.toString();
	}
}
