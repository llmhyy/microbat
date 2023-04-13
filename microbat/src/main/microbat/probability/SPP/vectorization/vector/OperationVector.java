package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.model.trace.TraceNode;

public class OperationVector extends Vector{
	private final boolean[] operation = new boolean[256];
	
	public OperationVector(final TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		for (int idx=0; idx<this.operation.length; idx++) {
			this.operation[idx] = false;
		}
		for (ByteCode byteCode : byteCodeList) {
			short opCode = byteCode.getOpcode();
			this.operation[opCode] = true;
		}
	}
	
	public OperationVector(final OperationVector otherVector) {
		for (int idx=0; idx<this.operation.length; idx++) {
			this.operation[idx] = otherVector.operation[idx];
		}
	}
	
	public boolean[] getOperationArray() {
		return this.operation;
	}
	
	public boolean equals(Object otherObj) {
		if (otherObj instanceof OperationVector) {
			OperationVector otherVec = (OperationVector) otherObj;
			if (!Arrays.equals(this.operation, otherVec.operation)) {
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
		strBuilder.append(this.operation[0] ? "1" : "0");
		for (int idx=1; idx<this.operation.length; ++idx) {
			strBuilder.append(",");
			strBuilder.append(this.operation[idx] ? "1" : "0");
		}
		return strBuilder.toString();
	}
}
