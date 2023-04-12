package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;

public class OperationVector {
	private final boolean[] operation = new boolean[256];
	private final boolean isCallingAPI;
	private final double apiConfidence;
	
	public OperationVector(final ByteCodeList byteCodeList, final boolean isCallingAPI, final double apiConfidence) {
		for (ByteCode byteCode : byteCodeList) {
			short opCode = byteCode.getOpcode();
			operation[opCode] = true;
		}
		this.isCallingAPI = isCallingAPI;
		this.apiConfidence = apiConfidence;
	}
	
	public OperationVector(final ByteCodeList byteCodeList, final boolean isCallingAPI) {
		this(byteCodeList, isCallingAPI, 0.5);
	}
	
	public boolean[] getOperationArray() {
		return this.operation;
	}
	
	public boolean isCallingAPI() {
		return this.isCallingAPI;
	}
	
	public double getAPIConfidence() {
		return this.apiConfidence;
	}
	
	public boolean equals(Object otherObj) {
		if (otherObj instanceof OperationVector) {
			OperationVector otherVec = (OperationVector) otherObj;
			if (!Arrays.equals(this.operation, otherVec.operation)) {
				return false;
			}
			if (this.isCallingAPI == otherVec.isCallingAPI) {
				return false;
			}
			if (this.apiConfidence == otherVec.apiConfidence) {
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
		strBuilder.append(",");
		strBuilder.append(this.isCallingAPI ? "1" : "0");
		strBuilder.append(",");
		strBuilder.append(String.format("%.2f", this.apiConfidence));
		return strBuilder.toString();
	}
}
