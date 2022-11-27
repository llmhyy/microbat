package microbat.probability.SPP.vectorization;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Vectorization of Trace Node
 * @author David
 */
public class NodeVector {
	private final boolean[] operation = new boolean[256];
	private final double APIconfidence;
	private final boolean[] source_vector = new boolean[16];
	private final double correctness_prob;
	private final boolean[] target_vector = new boolean[16];
	
	NodeVector(final TraceNode node, VarValue var, boolean forward) {
		
		if (node.getWrittenVariables().isEmpty() || node.getReadVariables().isEmpty()) {
			throw new IllegalArgumentException("[Error] NodeVector Construct: NodeVector will only vectorize the node with both read and written variables");
		}
		
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		for (ByteCode byteCode : byteCodeList) {
			short opCode = byteCode.getOpcode();
			operation[opCode] = true;
		}
		
		this.APIconfidence = 0.0;
		this.correctness_prob = 0.0;
	}
	
}
