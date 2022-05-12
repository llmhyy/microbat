package microbat.baseline.encoders;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.UniquePriorityQueue;
import microbat.model.BreakPoint;
import microbat.model.trace.ConstWrapper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.JavaUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
		this.matchInstruction();
		this.preEncode();
		this.setupFlag = true;
	}
	
	public void encode() {
		// on encoding the probabilities will change
		if (!setupFlag) {
			setup();
			return;
		}

		System.out.println("Start encoding probabilities");
		// Encode variable until there is no change to var probabilities
		boolean hasChange = false;
		int count = 1;
		do {
			probabilities = null;
			long start = System.currentTimeMillis();
			hasChange = new VariableEncoder(trace, executionList).encode();
			long end = System.currentTimeMillis();
			System.out.println("Iteration " + (count++) + ": " + (end - start) + "ms");
			try {
				// just to allow other operations to take place
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.err.println("Attempt to interrupt sleeping thread");
			}	
		} while(hasChange);
		
		// Use the probabilities from var to derive statement probability
		new StatementEncoder(trace, executionList).encode();
		System.out.println("Finish encoding probabilities");
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
			System.out.println();
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
			System.out.println(tn.getOrder());
			System.out.println(tn.getInstructions());
		}
	}
	
	private void matchInstruction() {
		HashMap<String, HashMap<Integer, ASTNode>> store = new HashMap<>();
		for (TraceNode tn: executionList) {
			BreakPoint breakpoint = tn.getBreakPoint();
			String sourceFile = breakpoint.getFullJavaFilePath();
			
			if (!store.containsKey(sourceFile)) {
				store.put(sourceFile, new HashMap<>());
				CompilationUnit cu = JavaUtil.findCompiltionUnitBySourcePath(sourceFile, 
						breakpoint.getDeclaringCompilationUnitName());
				cu.accept(new ASTVisitor() {
					private HashMap<Integer, ASTNode> specificStore = store.get(sourceFile);
					private int getLineNumber(ASTNode node) {
						return cu.getLineNumber(node.getStartPosition());
					}
					
					private void storeNode (int line, ASTNode node) {
						if (!specificStore.containsKey(line)) {
							specificStore.put(line, node);
						}
					}
					
					private void wrapperVisit(ASTNode node) {
						int lineNumber = getLineNumber(node);
						storeNode(lineNumber, node);
					}
					
					public void preVisit(ASTNode node) {
						wrapperVisit(node);
					}
					
//					public boolean visit(ArrayAccess node) {
//						return wrapperVisit(node);
//					}
//					
//					public boolean visit(Assignment node) {
//						return wrapperVisit(node);
//					}
//					
//					public boolean visit(ConditionalExpression node) {
//						return wrapperVisit(node);
//					}
//					
//					public boolean visit(FieldAccess node) {
//						return wrapperVisit(node);
//					}
//					
//					public boolean visit(IfStatement node) {
//						return wrapperVisit(node);
//					}
//					
//					public boolean visit(MethodInvocation node) {
//						return wrapperVisit(node);
//					}
//					
//					public boolean visit(InfixExpression node) {
//						return wrapperVisit(node);
//					}
				});

			}

			ASTNode node = store.get(sourceFile).getOrDefault(breakpoint.getLineNumber(), null);
			tn.setAstNode(node);
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
	
	private void printVarExhaustively(VarValue v, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(' ');
		}
		sb.append(v.toString());
		System.out.println(sb.toString());
		for (VarValue child : v.getChildren()) {
			printVarExhaustively(child, n + 2);
		}
	}
	
	// can consider moving this to Trace.java instead and return a Trace
	private static List<TraceNode> slice(Trace trace) {
		UniquePriorityQueue<TraceNode> toVisit = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		
		List<TraceNode> visitedNodes = new ArrayList<>();
		toVisit.addIgnoreNull(trace.getLatestNode());
		
		while (toVisit.size() > 0) {
			TraceNode node = toVisit.poll();
			if (node == null)
				continue; // has already been visited
			for (VarValue v : node.getReadVariables())
				toVisit.addIgnoreNull(trace.findDataDependency(node, v));
			toVisit.addIgnoreNull(node.getControlDominator());
			visitedNodes.add(node);
		}
		
		List<TraceNode> result = new ArrayList<>(visitedNodes.size());
		for (int i = visitedNodes.size(); i > 0; i--) {
			result.add(visitedNodes.get(i-1));
		}
		return result;
	}
}
