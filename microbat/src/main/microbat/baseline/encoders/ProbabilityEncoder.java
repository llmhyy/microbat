package microbat.baseline.encoders;

import microbat.baseline.Configs;
import microbat.model.BreakPoint;
import microbat.model.trace.ConstWrapper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;

public class ProbabilityEncoder {
	private StatementEncoder stmtEncoder;
	private VariableEncoder varEncoder;
	private HashMap<BreakPoint, Integer> invocationTable;
	private Trace trace;
	
	public ProbabilityEncoder(Trace trace) {
		this.trace = trace;
		this.invocationTable = new HashMap<>();
		this.stmtEncoder = new StatementEncoder();
		this.varEncoder = new VariableEncoder(trace);
	}
	
	public void encode() {
		matchInstructions();
		preEncode();
		System.out.println("Start encoding probabilities");
		this.varEncoder.encode();
	}
	
	private void matchInstructions() {
		for (TraceNode tn : this.trace.getExecutionList()) {
			System.out.println(tn.getOrder());
//			HashMap<Integer, ConstWrapper> constPool = tn.getConstPool();
			for (String varName: tn.getStackVariables()) {
				System.out.println(varName);
			}
			List<InstructionHandle> instructions = tn.getInstructions();
			int i = 0;
			if (invocationTable.containsKey(tn.getBreakPoint())) {
				// TODO: Handle case for static methods
				i = invocationTable.get(tn.getBreakPoint());
				System.out.println("Returning from table");
			}
			List<InstructionHandle> tracedInstructions = new ArrayList<>();
			for (; i < instructions.size(); i++) {
				InstructionHandle ih = instructions.get(i);
				tracedInstructions.add(ih);
				System.out.print(ih);
//				if (ih.getInstruction() instanceof CPInstruction) {
//					ConstWrapper c = constPool.get(((CPInstruction) ih.getInstruction()).getIndex());
//					System.out.print(c);
//				}
//				System.out.println();
				if (ih.getInstruction() instanceof InvokeInstruction && tn.getInvocationChildren().size() > 0) {
					this.invocationTable.put(tn.getBreakPoint(), i+1);
					System.out.println("Storing in table");
					System.out.println(this.invocationTable);
					break;
				}
			}
			tn.setInstructions(tracedInstructions);
		}
	}
	
	private void preEncode() {
		/* 
		 * method before encoding that does:
		 * 1. set all input variable to be HIGH
		 * 2. set all output variable to be LOW
		*/
		List<TraceNode> executionList = this.trace.getExecutionList();
		
		// Set read variable to HIGH
		for (VarValue v : executionList.get(0).getReadVariables()) {
			v.setProbability(Configs.HIGH);
		}
		
		for (VarValue v : executionList.get(executionList.size() - 1).getWrittenVariables()) {
			v.setProbability(Configs.LOW);
		}
	}
}
