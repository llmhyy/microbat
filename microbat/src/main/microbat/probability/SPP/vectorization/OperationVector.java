package microbat.probability.SPP.vectorization;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.model.trace.TraceNode;

public class OperationVector {
	private final boolean[] operation = new boolean[256];
	private final boolean isCallingAPI;
	private final double apiConfidence;
	
	public OperationVector(final TraceNode node, final double apiConfidence) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		for (ByteCode byteCode : byteCodeList) {
			short opCode = byteCode.getOpcode();
			operation[opCode] = true;
		}
		this.isCallingAPI = false;
		this.apiConfidence = 0.0;
	}
}
