package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.model.trace.TraceNode;

public class OperationVector extends Vector {
	
	public static final int DIMENSION = 256;
	
	public OperationVector() {
		super(OperationVector.DIMENSION);
	}
	
	public OperationVector(final TraceNode node) {
		super(OperationVector.DIMENSION);
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		for (ByteCode byteCode : byteCodeList) {
			short opCode = byteCode.getOpcode();
			this.vector[opCode] += 1.0f;
		}
	}
}
