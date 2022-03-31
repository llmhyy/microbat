package microbat.baseline.encoders;

import microbat.baseline.Configs;
import microbat.model.BreakPoint;
import microbat.model.trace.ConstWrapper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;

public class ProbabilityEncoder {
	private HashMap<BreakPoint, Integer> invocationTable;
	private Trace trace;
	private List<TraceNode> executionList;
	private PriorityQueue<TraceNode> probabilities;
	private boolean setupFlag = false;
	
	public ProbabilityEncoder(Trace trace) {
		this.trace = trace;
		// we will only operate on a slice of the program to save time
		this.executionList = slice(trace);
		this.invocationTable = new HashMap<>();
	}
	
	public void setup() {
		/*
		 *  this method should only be called when resetting
		 *  the probability encode (i.e. when re-running without
		 *  previous run information)
		 */
		this.matchInstructions();
		this.preEncode();
		this.setupFlag = true;
		
	}
	
	public void encode() {
		// on encoding the probabilities will change
		if (!setupFlag)
			setup();
		System.out.println("Start encoding probabilities");
		boolean variableBool, statementBool;
		int count = 1;
		do {
			probabilities = null;
			long start = System.currentTimeMillis();
			variableBool = new VariableEncoder(trace, executionList).encode();
			statementBool = new StatementEncoder(trace, executionList).encode();
			long end = System.currentTimeMillis();
			System.out.println("Iteration " + (count++) + ": " + (end - start) + "ms");
			System.out.println("Most Erroneous Statement: " + getMostErroneousNode().getOrder());
			try {
				// just to allow other operations to take place
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.err.println("Attempt to interrupt sleeping thread");
			}
		// TODO: Remove count to let it terminate on its own	
		} while(count < 100 && (variableBool || statementBool));
		printProbability();
	}
	
	public void printProbability() {
		for (TraceNode tn : executionList) {
			System.out.println(String.format("Order %d: %f", tn.getOrder(), tn.getProbability()));
			System.out.println("---Read---");
			for (VarValue v : tn.getReadVariables()) {
				System.out.print(v.getVarName() + ": ");
				System.out.println(v.getProbability());
			}
			
			System.out.println("---Write---");
			for (VarValue v : tn.getWrittenVariables()) {
				System.out.print(v.getVarName() + ": ");
				System.out.println(v.getProbability());
			}
		}
	}
	
	public TraceNode getMostErroneousNode() {
		if (probabilities == null)
			populatePriorityQueue();
		return probabilities.peek();
	}
	
	private void populatePriorityQueue() {
		probabilities = new PriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return Double.compare(t1.getProbability(), t2.getProbability());
			}
		});
		probabilities.addAll(executionList);
	}
	
	private void matchInstructions() {
		for (TraceNode tn : this.trace.getExecutionList()) {
//			HashMap<Integer, ConstWrapper> constPool = tn.getConstPool();
//			for (VarValue v : tn.getReadVariables()) {
//				System.out.println(v.getAllDescedentChildren());
//			}
			List<InstructionHandle> instructions = tn.getInstructions();
			int i = 0;
			if (invocationTable.containsKey(tn.getBreakPoint())) {
				// TODO: Handle case for static methods
				i = invocationTable.get(tn.getBreakPoint());
			}
			List<InstructionHandle> tracedInstructions = new ArrayList<>();
			for (; i < instructions.size(); i++) {
				InstructionHandle ih = instructions.get(i);
				tracedInstructions.add(ih);
//				if (ih.getInstruction() instanceof CPInstruction) {
//					ConstWrapper c = constPool.get(((CPInstruction) ih.getInstruction()).getIndex());
//					System.out.print(c);
//				}
				if (ih.getInstruction() instanceof InvokeInstruction && tn.getInvocationChildren().size() > 0) {
					this.invocationTable.put(tn.getBreakPoint(), i+1);
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
		
		
		for (int i = 0; i < executionList.size(); i++) {
			TraceNode tn = executionList.get(i);
			tn.setProbability(Configs.UNCERTAIN);
			
			for (VarValue v : tn.getReadVariables()) {
				// Set initial read variable to HIGH
				v.setProbability(i == 0 ? Configs.HIGH : Configs.UNCERTAIN);
			}
			
			for (VarValue v : tn.getWrittenVariables()) {
				// Set final written variable to LOW
				v.setProbability(i == executionList.size() - 1 ? Configs.LOW : Configs.UNCERTAIN);
			}
		}
	}
	
	// can consider moving this to Trace.java instead and return a Trace
	private static List<TraceNode> slice(Trace trace) {
		HashSet<TraceNode> nodes = new HashSet<>();
		Deque<TraceNode> toVisit = new LinkedList<>();
		toVisit.add(trace.getLatestNode());
		while (toVisit.size() > 0) {
			TraceNode node = toVisit.poll();
			if (nodes.contains(node) || node == null)
				continue; // has already been visited
			for (VarValue v : node.getReadVariables())
				toVisit.add(trace.findDataDependency(node, v));
			toVisit.add(node.getControlDominator());
			nodes.add(node);
		}
		
		List<TraceNode> result = new ArrayList<>(nodes);
		Collections.sort(result, new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t1.getOrder() - t2.getOrder();
			}
		});
		return result;
	}
}
