package microbat.baseline.encoders;

import microbat.baseline.Configs;
import microbat.baseline.UniquePriorityQueue;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class ProbabilityEncoder {
	private HashMap<BreakPoint, Integer> invocationTable;
	private Trace trace;
	private List<TraceNode> executionList;
	private PriorityQueue<TraceNode> probabilities;
	private boolean setupFlag = false;
	
//	private final int maxItr;
	
	private static List<NodeFeedbackPair> userFeedbacks = new ArrayList<>();
	
	private List<VarValue> outputVars;
	private List<VarValue> inputVars;
	
	public ProbabilityEncoder(Trace trace) {
		System.out.println("Initialize Probability Encoder");
		this.trace = trace;
		// we will only operate on a slice of the program to save time
		this.executionList = slice(trace);
//		System.out.print("slice: ");
//		for (TraceNode node : this.executionList) {
//			System.out.print(node.getOrder() + " ");
//		}
//		System.out.println();
//		List<TraceNode> slice = new ArrayList<>();
//		for (int order = 5; order <= 6; order++) {
//			slice.add(this.trace.getTraceNode(order));
//		}
//		this.executionList = slice;
		// this.executionList = this.trace.getExecutionList();
		this.invocationTable = new HashMap<>();
		
		this.inputVars = this.getInputVariables(executionList);
		this.outputVars = this.getOutputVariables(executionList);
		
		this.modifyVarID();
		
		// Maximum iteration allowed if the variable probability do not converge
//		this.maxItr = 10;
	}
	
	public void setup() {
		/*
		 *  this method should only be called when resetting
		 *  the probability encode (i.e. when re-running without
		 *  previous run information)
		 */
		// this.matchInstruction();
		this.preEncode();
		ProbabilityEncoder.clearFeedbacks();
		this.setupFlag = true;
	}
	
	public void setFlag(boolean setupFlag) {
		this.setupFlag = setupFlag;
	}
	
	public void encode() {
		// on encoding the probabilities will change
		if (!setupFlag) {
			setup();
			return;
		}
		
		VariableEncoder varEncoder = new VariableEncoder(trace, executionList, this.inputVars, this.outputVars);
		varEncoder.setFeedbacks(ProbabilityEncoder.userFeedbacks);
		varEncoder.encode();
		
		new StatementEncoder(trace, executionList).encode();
//		System.out.println("Start encoding probabilities");
//		// Encode variable until there is no change to var probabilities
//		boolean hasChange = false;
//		int count = 1;
//		do {
//			probabilities = null;
//			long start = System.currentTimeMillis();
//			hasChange = new VariableEncoder2(trace, executionList, this.outputVars, this.inputVars).encode();
//			long end = System.currentTimeMillis();
//			System.out.println("Iteration " + (count++) + ": " + (end - start) + "ms");
//			try {
//				// just to allow other operations to take place
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				System.err.println("Attempt to interrupt sleeping thread");
//			}
////			break;
//		} while(hasChange && count <= this.maxItr);
//		
//		// Use the probabilities from var to derive statement probability
//		new StatementEncoder(trace, executionList).encode();
//		System.out.println("Finish encoding probabilities");
	}
	
	private List<VarValue> getInputVariables(List<TraceNode> executionList) {
		List<VarValue> inputVars = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			for (VarValue readVar : node.getReadVariables()) {
				if (readVar.getVarName().contains("input")) {
					boolean alreadyInside = false;
					for (VarValue input : inputVars) {
						if (input.getVarName().equals(input.getVarName())) {
							alreadyInside = true;
						}
					}
					if (!alreadyInside) {
						inputVars.add(readVar);
					}
				}
			}
		}
		return inputVars;
	}
	
	private List<VarValue> getOutputVariables(List<TraceNode> executionList) {
		List<VarValue> outputVars = new ArrayList<>();
		for (int order=executionList.size()-1; order > 0; order--) {
			TraceNode node = this.executionList.get(order);
			if (!node.getWrittenVariables().isEmpty()) {
				for (VarValue writeVar : node.getWrittenVariables()) {
					outputVars.add(writeVar);
				}
				break;
			}
		}
		return outputVars;
	}
	
	public static void addFeedback(NodeFeedbackPair newFeedback) {
		// Check is the node already have feedback. If yes, update feedback
		boolean found = false;
		for (NodeFeedbackPair pair : ProbabilityEncoder.userFeedbacks) {
			if (pair.reviewingSameNode(newFeedback)) {
				pair.setFeedback(newFeedback.getFeedback());
				found = true;
				break;
			}
		}
		if (!found) {
			ProbabilityEncoder.userFeedbacks.add(newFeedback);
		}
	}
	
	public static void clearFeedbacks() {
		ProbabilityEncoder.userFeedbacks.clear();
	}
	
	public static int getFeedbackCount() {
		return ProbabilityEncoder.userFeedbacks.size();
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
	
	private void modifyVarID() {
		Map<String, String> mapping = new HashMap<>();
		for (TraceNode node : this.executionList) {
			System.out.println("chekcing: " + node.getOrder());
			for (VarValue readVar : node.getReadVariables()) {
				String varID = readVar.getVarID();
				if (!mapping.containsKey(varID)) {
					mapping.put(varID, varID);
				} else {
					String newID = mapping.get(varID);
					readVar.setVarID(newID);
				}
			}
			
			for (VarValue writeVar : node.getWrittenVariables()) {
				String varID = writeVar.getVarID();
				if (mapping.containsKey(varID)) {
					System.out.println("has Change");
					String newID = writeVar.getVarID() + "-" + node.getOrder();
					mapping.put(varID, newID);
					
					writeVar.setVarID(newID);
				}
			}
		}
	}
	
	/**
	 * Update probability based on the feedback.
	 * @param node Trace node that the feedback referring to.
	 * @param feedback User Feedback
	 */
//	public void updateProbability(TraceNode node, UserFeedback feedback) {
//		switch (feedback.getFeedbackType()) {
//		case UserFeedback.UNCLEAR:
//			break;
//		case UserFeedback.CORRECT:
//			this.updateNode(node, Configs.HIGH);
//			break;
//		case UserFeedback.WRONG_PATH:
//			node.getControlDominator().setProbability(Configs.LOW);
//			this.updateNode(node, Configs.UNCERTAIN);
//			break;
//		case UserFeedback.WRONG_VARIABLE_VALUE:
////			node.setProbability(Configs.LOW);
//			feedback.getOption().getReadVar().setProbability(Configs.LOW);
//			break;
//		default:
//			break;
//		}
//	}
	
	/**
	 * Update the probability of all factor of given node
	 * @param node Trace node to be updated
	 * @param prob New probability
	 */
//	private void updateNode(TraceNode node, double prob) {
//		node.setProbability(prob);
//		this.setProb(node.getReadVariables(), prob);
//		this.setProb(node.getWrittenVariables(), prob);
//	}
	
	private void populatePriorityQueue() {
		probabilities = new PriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return Double.compare(t1.getProbability(), t2.getProbability());
			}
		});
		probabilities.addAll(executionList);
	}
	
	/**
	 * Initialize the correctness probability of variable and statement
	 */
	private void preEncode() {
//		boolean finishSettingInput = false;
//		for (int order=0; order<this.executionList.size(); ++order) {
//			
//			TraceNode node = this.executionList.get(order);
//			node.setProbability(Configs.HIGH);
//			node.setPredProb(Configs.HIGH);
//			
//			// Set the input (read variable in first node) to be High
//			if (!finishSettingInput) {
//				if (node.getReadVariables().isEmpty()) {
//					this.setProb(node.getWrittenVariables(), Configs.UNCERTAIN);
//					continue;
//				} else {
//					this.setProb(node.getWrittenVariables(), Configs.UNCERTAIN);
//					this.setProb(node.getReadVariables(), Configs.HIGH);
//
//					finishSettingInput = true;
//					continue;
//				}
//			}
//		
//			// Set the other variable to be Uncertain
//			this.setProb(node.getWrittenVariables(), Configs.UNCERTAIN);
//			this.setProb(node.getReadVariables(), Configs.UNCERTAIN);
//		}
//		
//		// Set the output (write variables in last trace node) to be Low
//		for (int order=this.executionList.size()-1; order > 0; order--) {
//			
//			TraceNode node = this.executionList.get(order);
//			if (!node.getWrittenVariables().isEmpty()) {
//				// Don't use setProb here because setProb only change probability when the probility is not set before
//				for (VarValue writeVar : node.getWrittenVariables()) {
//					writeVar.setProbability(Configs.LOW);
//				}
//				break;
//			}
//			
//		}
		
		for (TraceNode node : this.trace.getExecutionList()) {
			node.setProbability(Configs.UNCERTAIN);
			for (VarValue readVar : node.getReadVariables()) {
				if (this.inputVars.contains(readVar)) {
					readVar.setProbability(Configs.HIGH);
				} else {
					readVar.setProbability(Configs.UNCERTAIN);
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				if (this.outputVars.contains(writeVar)) {
					writeVar.setProbability(Configs.LOW);
				} else {
					writeVar.setProbability(Configs.UNCERTAIN);
				}
			}
		}
		for (TraceNode node : this.executionList) {
			System.out.println("Init node: " + node.getOrder());
			for (VarValue readVar : node.getReadVariables()) {
				System.out.println("Init readVar : " + readVar.getVarName() + " with prob = " + readVar.getProbability());
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				System.out.println("Init writeVar: " + writeVar.getVarName() + " with prob = " + writeVar.getProbability());
			}
		}
	}
	
//	private void setProb(List<VarValue> vars, double prob) {
//		for (VarValue var : vars) {
//			if (var.getProbability() == 0) {
//				var.setProbability(prob);
//			}
//		}
//	}
	
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
	
	/**
	 * Perform dynamic slicing so that we only consider the data dependents of the wrong variable
	 * @param trace Complete trace
	 * @return sliced execution list
	 */
	private static List<TraceNode> slice(Trace trace) {
		UniquePriorityQueue<TraceNode> toVisit = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		
		List<TraceNode> visitedNodes = new ArrayList<>();
		
		TraceNode latestNode = trace.getLatestNode();
		if (latestNode.getCodeStatement().equals("}")) {
			latestNode = trace.getTraceNode(trace.size() - 1);
		}
		toVisit.addIgnoreNull(latestNode);
		
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
	
//	private void matchInstructions() {
//	for (TraceNode tn : this.trace.getExecutionList()) {
////		HashMap<Integer, ConstWrapper> constPool = tn.getConstPool();
////		for (VarValue v : tn.getReadVariables()) {
////			System.out.println(v.getAllDescedentChildren());
////		}
//		List<InstructionHandle> instructions = tn.getInstructions();
//		int i = 0;
//		if (invocationTable.containsKey(tn.getBreakPoint())) {
//			// TODO: Handle case for static methods
//			i = invocationTable.get(tn.getBreakPoint());
//		}
//		List<InstructionHandle> tracedInstructions = new ArrayList<>();
//		for (; i < instructions.size(); i++) {
//			InstructionHandle ih = instructions.get(i);
//			tracedInstructions.add(ih);
////			if (ih.getInstruction() instanceof CPInstruction) {
////				ConstWrapper c = constPool.get(((CPInstruction) ih.getInstruction()).getIndex());
////				System.out.print(c);
////			}
//			if (ih.getInstruction() instanceof InvokeInstruction && tn.getInvocationChildren().size() > 0) {
//				this.invocationTable.put(tn.getBreakPoint(), i+1);
//				break;
//			}
//		}
//		tn.setInstructions(tracedInstructions);
//		System.out.println(tn.getOrder());
//		System.out.println(tn.getInstructions());
//	}
//}
//
//private void matchInstruction() {
//	HashMap<String, HashMap<Integer, ASTNode>> store = new HashMap<>();
//	for (TraceNode tn: executionList) {
//		BreakPoint breakpoint = tn.getBreakPoint();
//		String sourceFile = breakpoint.getFullJavaFilePath();
//		
//		if (!store.containsKey(sourceFile)) {
//			store.put(sourceFile, new HashMap<>());
//			CompilationUnit cu = JavaUtil.findCompiltionUnitBySourcePath(sourceFile, 
//					breakpoint.getDeclaringCompilationUnitName());
//			cu.accept(new ASTVisitor() {
//				private HashMap<Integer, ASTNode> specificStore = store.get(sourceFile);
//				private int getLineNumber(ASTNode node) {
//					return cu.getLineNumber(node.getStartPosition());
//				}
//				
//				private void storeNode (int line, ASTNode node) {
//					if (!specificStore.containsKey(line)) {
//						specificStore.put(line, node);
//					}
//				}
//				
//				private void wrapperVisit(ASTNode node) {
//					int lineNumber = getLineNumber(node);
//					storeNode(lineNumber, node);
//				}
//				
//				public void preVisit(ASTNode node) {
//					wrapperVisit(node);
//				}
//				
////				public boolean visit(ArrayAccess node) {
////					return wrapperVisit(node);
////				}
////				
////				public boolean visit(Assignment node) {
////					return wrapperVisit(node);
////				}
////				
////				public boolean visit(ConditionalExpression node) {
////					return wrapperVisit(node);
////				}
////				
////				public boolean visit(FieldAccess node) {
////					return wrapperVisit(node);
////				}
////				
////				public boolean visit(IfStatement node) {
////					return wrapperVisit(node);
////				}
////				
////				public boolean visit(MethodInvocation node) {
////					return wrapperVisit(node);
////				}
////				
////				public boolean visit(InfixExpression node) {
////					return wrapperVisit(node);
////				}
//			});
//
//		}
//
//		ASTNode node = store.get(sourceFile).get(breakpoint.getLineNumber());
//		tn.setAstNode(node);
//	}
//}
}
