package microbat.probability.SPP.vectorization;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.SPP.vectorization.vector.NodeVector_1;

public class TraceVectorizer {

	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	
	public TraceVectorizer() {
		this.constructUnmodifiedOpcodeType();
	}
	
	private void constructUnmodifiedOpcodeType() {
		this.unmodifiedType.add(OpcodeType.LOAD_CONSTANT);
		this.unmodifiedType.add(OpcodeType.LOAD_FROM_ARRAY);
		this.unmodifiedType.add(OpcodeType.LOAD_VARIABLE);
		this.unmodifiedType.add(OpcodeType.STORE_INTO_ARRAY);
		this.unmodifiedType.add(OpcodeType.STORE_VARIABLE);
		this.unmodifiedType.add(OpcodeType.RETURN);
	}
	
	public List<NodeVector_1> vectorize(final Trace trace) {
		this.preprocess(trace);
		List<NodeVector_1> vectors = new ArrayList<>();
		for (TraceNode node : trace.getExecutionList()) {
			for (VarValue readVar : node.getReadVariables()) {
				NodeVector_1 vector = new NodeVector_1(node, readVar, true);
				vectors.add(vector);
			}
		}
		return vectors;
	}
	
	private void preprocess(final Trace trace) {
		this.countRepeat(trace);
		this.computeCost(trace);
	}
	
	private void countRepeat(final Trace trace) {
		Map<BreakPoint, Integer> repeatedCounts = new HashMap<>();
		for (TraceNode node : trace.getExecutionList()) {
			final BreakPoint bkp = node.getBreakPoint();
			repeatedCounts.put(bkp, 
				repeatedCounts.containsKey(bkp) ?
				repeatedCounts.get(bkp) + 1:
				0
			);
			node.repeatedCount = repeatedCounts.get(bkp);
		}
	}
	
	private void computeCost(final Trace trace) {
		int totalNodeCost = 0;
		for (TraceNode node : trace.getExecutionList()) {

		}
	}
	
	private int countModifyOperation(final TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		int count = 0;
		for (ByteCode byteCode : byteCodeList) {
			if (!this.unmodifiedType.contains(byteCode.getOpcodeType())) {
				count+=1;
			}
		}
		return count;
	}
}
