package microbat.baseline.encoders;

import microbat.baseline.Configs;
import microbat.baseline.UniquePriorityQueue;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.VariableConstraint;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.recommendation.UserFeedback;
import tracediff.TraceDiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import jmutation.MutationFramework;

/**
 * Probability Encoder follow the procedure in ICSE18 paper
 * It will first calculate probability of correctness of variable using sum-product algorithm,
 * and then calculate of correctness of each statement by brute force
 * @author David, Siang Hwee 
 *
 */
public class ProbabilityEncoder {
	
	/*
	 * Prefix of condition result ID
	 */
	public static final String CONDITION_RESULT_ID_PRE = "CR_";
	
	/*
	 * Prefix of condition result variable name
	 */
	public static final String CONDITION_RESULT_NAME_PRE = "ConditionResult_";
	
	/**
	 * The complete trace of execution of buggy program
	 */
	private Trace trace;
	
	/**
	 * Trace after dynamic slicing based on the output variable
	 */
	private List<TraceNode> executionList;
	
	/**
	 * List of trace node ordered by probability of correctness
	 */
	private PriorityQueue<TraceNode> probabilities;
	
	/**
	 * True if the environment has already been set up
	 */
	private boolean setupFlag = false;

	/**
	 * List of user feedback on correspondence node
	 */
	private static List<NodeFeedbackPair> userFeedbacks = new ArrayList<>();
	
	/**
	 * Output variables of the program, which assumed to be wrong
	 */
	private List<VarValue> outputVars;
	
	/**
	 * Input variables of the program, which assume to be correct
	 */
	private List<VarValue> inputVars;
	
	/**
	 * Constructor
	 * @param trace Trace of testing program
	 */
	public ProbabilityEncoder(Trace trace) {

		this.trace = trace;
		
		// we will only operate on a slice of the program to save time
//		this.executionList = slice(trace);
		this.executionList = null;
		this.outputVars = null;
		this.inputVars = null;
	}
	
	/**
	 * Do pre-processing on the given trace:
	 * 1. Clear all the feedbacks
	 * 2. Add the condition result for branch node
	 * 
	 * This method should only be called when resetting the probability encoder
	 * because it will clean up the previous run information
	 */
	public void setup() {
		
		/*
		 * If no information about input and output, then we will
		 * determine it in default way, which is not recommended.
		 */
		if (this.inputVars == null || this.outputVars == null) {
			this.inputVars = this.extractDefaultInputVariables(this.trace.getExecutionList());
			this.outputVars = this.extractDefaultOutputVariables(this.trace.getExecutionList());
		}
		
		if (this.executionList == null) {
			this.executionList = this.dynamicSlicing(this.trace, this.outputVars);
		}
		
		// Clear all previous feedbacks
		ProbabilityEncoder.clearFeedbacks();

		// Remove this variable
		this.removeThisVar(executionList);

		// Solve the problem of same variable ID after re-definition
		this.changeArrayElementID(this.executionList);
		this.changeRedefinitionID(this.executionList);
		
		// Add condition result as control predicate
		this.addConditionResult(trace.getExecutionList());

		// Reset ID of all kind of constraint
		Constraint.resetID();
		
		this.setupFlag = true;
	}
	
	/**
	 * Set the set up flag
	 * @param setupFlag Set to True if the probability encoder do not need to be reset
	 */
	public void setFlag(boolean setupFlag) {
		this.setupFlag = setupFlag;
	}
	
	/**
	 * Calculate the probability of the variable and statements
	 */
	public void encode() {
		
		/*
		 * If no information about input and output, then we will
		 * determine it in default way, which is not recommended.
		 */
		if (this.inputVars == null || this.outputVars == null) {
			this.inputVars = this.extractDefaultInputVariables(this.trace.getExecutionList());
			this.outputVars = this.extractDefaultOutputVariables(this.trace.getExecutionList());
		}
		
		if (this.executionList == null) {
			this.executionList = this.dynamicSlicing(this.trace, this.outputVars);
		}
		
		if (!setupFlag) {
			setup();
			return;
		}
		
		VariableEncoder varEncoder = new VariableEncoder(trace, executionList, this.inputVars, this.outputVars);
		
		// Include all the previous users feedback
		varEncoder.setFeedbacks(ProbabilityEncoder.userFeedbacks);
		
		long startTime = System.currentTimeMillis();
		
		// Calculate the probability for variables
		varEncoder.encode();	
		
		// Calculate the probability for statements
		new StatementEncoder(trace, executionList).encode();
		
		long endTime = System.currentTimeMillis();
		
		long executionTime = Math.floorDiv(endTime - startTime, 1000);
		System.out.println("Execution Time: " + executionTime + "s");
	}
	
	/**
	 * Determine the input variables by brute force
	 * 
	 * Input variables are defined to be the read variables with the name contain "input"
	 * @param executionList The execution trace of program
	 * @return List of input variables
	 */
	private List<VarValue> extractDefaultInputVariables(List<TraceNode> executionList) {
		List<VarValue> inputVars = new ArrayList<>();
		for (TraceNode node : executionList) {
			for (VarValue readVar : node.getReadVariables()) {
				if (readVar.getVarName().contains("input")) {
					boolean alreadyInside = false;
					for (VarValue input : inputVars) {
						if (readVar.getVarID().equals(input.getVarID())) {
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
	
	/**
	 * Determine the output variables by brute force
	 * 
	 * Output variables are defined to be the last written variable
	 * @param executionList
	 * @return
	 */
	private List<VarValue> extractDefaultOutputVariables(List<TraceNode> executionList) {
		List<VarValue> outputVars = new ArrayList<>();
		for (int order=executionList.size()-1; order > 0; order--) {
			TraceNode node = executionList.get(order);
			if (!node.getWrittenVariables().isEmpty()) {
				for (VarValue writeVar : node.getWrittenVariables()) {
					outputVars.add(writeVar);
				}
				break;
			}
		}
		return outputVars;
	}
	
	/**
	 * Add the users feedback 
	 * @param newFeedback Feedback of corresponding node
	 */
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
	
	/**
	 * Remove all previous feedbacks. The function is used when resetting the probability encoder
	 */
	public static void clearFeedbacks() {
		ProbabilityEncoder.userFeedbacks.clear();
	}
	
	/**
	 * Get the number of feedbacks used
	 * @return Number of feedbacks
	 */
	public static int getFeedbackCount() {
		return ProbabilityEncoder.userFeedbacks.size();
	}
	
	/**
	 * Access all the feedback of corresponding node
	 * @return List of node-feedback pair
	 */
	public static List<NodeFeedbackPair> getFeedbacks() {
		return ProbabilityEncoder.userFeedbacks;
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
	
	/**
	 * Get the trace node with the highest probability to be wrong
	 * @return Predicted wrong node
	 */
	public TraceNode getMostErroneousNode() {
//		if (probabilities == null)
//			populatePriorityQueue();
//		return probabilities.peek();
		
		TraceNode errorNode = this.executionList.get(0);
		for(TraceNode node : this.executionList) {
			if (node.getProbability() <= errorNode.getProbability()) {
				errorNode = node;
			}
		}
		return errorNode;
	}
	
	public List<TraceNode> getSlicedExecutionList() {
		return this.executionList;
	}
	
	public void setInputVars(List<VarValue> inputs) {
		this.inputVars = inputs;
	}
	
	public void setOutputVars(List<VarValue> outputs) {
		this.outputVars = outputs;
	}
	
	public List<VarValue> getInputVars() {
		return this.inputVars;
	}
	
	public List<VarValue> getOutputVars() {
		return this.outputVars;
	}
	
	/**
	 * Change the ID of variable that has been redefined so that they are consider to be different variable.
	 * This function is called when setting up the probability encoder
	 * 
	 * The new ID for redefined variable are the origin ID followed by the trace node order where they are redefined.
	 * @param executionList Execution trace of testing program
	 */
	private void changeRedefinitionID(List<TraceNode> executionList) {
		Map<String, String> mapping = new HashMap<>();
		for (TraceNode node : executionList) {

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
					String newID = writeVar.getVarID() + "-" + node.getOrder();
					mapping.put(varID, newID);
					
					writeVar.setVarID(newID);
				}
			}
		}
	}
	
	private void changeArrayElementID(List<TraceNode> executionList) {
		Map<String, String> addressMap = new HashMap<>();
		for (TraceNode node : executionList) {
			List<VarValue> vars = new ArrayList<>();
			vars.addAll(node.getReadVariables());
			vars.addAll(node.getWrittenVariables());
			for (VarValue var : vars) {
				if (!var.getChildren().isEmpty()) {
					addressMap.put(var.getAliasVarID(), var.getVarID());
				}
			}
		}
		
		for (TraceNode node : executionList) {
			List<VarValue> vars = new ArrayList<>();
			vars.addAll(node.getReadVariables());
			vars.addAll(node.getWrittenVariables());
			for (VarValue var : vars) {
				if (!var.getParents().isEmpty()) {
					String address = this.extractAddressFromElementID(var.getVarID());
					if (address != null) {
						var.setVarID(addressMap.get(address));
					}
				}
			}
		}
	}
	
	private String extractAddressFromElementID(final String id) {
		if (id.indexOf('[')==-1) {
			return null;
		}
		return id.substring(0, id.indexOf('['));
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
	
	private void removeThisVar(List<TraceNode> executionList) {
		for (TraceNode node : executionList) {
			List<Integer> thisIdxes = new ArrayList<>();
			for (int idx=0; idx < node.getReadVariables().size(); ++idx) {
				VarValue readVar = node.getReadVariables().get(idx);
				if (readVar.getVarName().equals("this")) {
					thisIdxes.add(idx);
				}
			}
			List<VarValue> readVars = node.getReadVariables();
			for (int thisIdx : thisIdxes) {
				readVars.remove(thisIdx);
			}
			node.setReadVariables(readVars);
			
			thisIdxes.clear();
			for (int idx=0; idx<node.getWrittenVariables().size(); ++idx) {
				VarValue writeVar = node.getWrittenVariables().get(idx);
				if (writeVar.getVarName().equals("this")) {
					thisIdxes.add(idx);
				}
			}
			List<VarValue> writeVars = node.getWrittenVariables();
			for (int thisIdx : thisIdxes) {
				writeVars.remove(thisIdx);
			}
			node.setWrittenVariables(writeVars);
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
	
	/**
	 * Perform dynamic slicing based on the outputs variables 
	 * @param outputs Output variables for the testing program
	 * @return Execution list after dynamic slicing
	 */
	private List<TraceNode> dynamicSlicing(Trace trace, List<VarValue> outputs) {

		/*
		 * Get trace node that contain the output variable and store them into 
		 * toVisit Node list
		 * 
		 * In case that there are multiple trace node that contain the same output variable,
		 * we only consider the last one. (Since the other will be included during dynamic
		 * slicing
		 */
		Queue<TraceNode> toVisitNodes = new LinkedList<>();
		List<VarValue> outputsCopy = new ArrayList<>();
		outputsCopy.addAll(outputs);
		// Search in reversed order because output usually appear at the end
		for (int order = trace.getLatestNode().getOrder(); order>=1; order--) {
			TraceNode node = trace.getTraceNode(order);
			List<VarValue> vars = new ArrayList<>();
			vars.addAll(node.getReadVariables());
			vars.addAll(node.getWrittenVariables());
			
			Iterator<VarValue> iter = outputsCopy.iterator();
			while(iter.hasNext()) {
				VarValue output = iter.next();
				if (vars.contains(output)) {
					toVisitNodes.add(node);
					iter.remove();
				}
			}
			// Case that all the output trace node is found
			if (outputsCopy.isEmpty()) {
				break;
			}
		}
		
		// Perform dynamic slicing base starting from output trace node
		// Just like doing breath first search
		Set<TraceNode> slicingSet = new HashSet<>();
		while (!toVisitNodes.isEmpty()) {
			TraceNode node = toVisitNodes.poll();
			for (VarValue readVar : node.getReadVariables()) {
				TraceNode dataDom = trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					toVisitNodes.add(dataDom);
				}
			}
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				toVisitNodes.add(controlDom);
			}
			
			slicingSet.add(node);
		}
		
		List<TraceNode> result = new ArrayList<>(slicingSet);
		Collections.sort(result, new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode node1, TraceNode node2) {
				return node1.getOrder() - node2.getOrder();
			}
		});
		return result;
	}
	
	/**
	 * Current microbat miss the condition result for each condition trace node.
	 * This function will add them back.
	 * 
	 * Currently, this function just naively define the result of condition by looking
	 * at the line number of next execution trace node.
	 * 
	 * If the line number is exactly the next line of the program, then the condition
	 * result will be True
	 * 
	 * If the trace skip the next line, then the condition result will be False
	 * @param executionList
	 */
	private void addConditionResult(List<TraceNode> executionList) {
		TraceNode branchNode = null;
		
		for (TraceNode node : executionList) {
			if (node.isBranch()) {
				if (branchNode != null) {
					this.addConditionResult(branchNode, node.getLineNumber() == branchNode.getLineNumber()+1);
				}
				branchNode = node;
				
			} else if (branchNode != null) {
				this.addConditionResult(branchNode, node.getLineNumber() == branchNode.getLineNumber()+1);
				branchNode = null;
			}
		}
	}
	
	// Helper function that add condition result
	private void addConditionResult(TraceNode node, boolean isResultTrue) {
		final String type = "boolean";
		final String varID = this.genConditionResultID(node.getOrder());
		final String varName = this.genConditionResultName(node.getOrder());
		Variable variable = new LocalVar(varName, type, "", node.getLineNumber());
		VarValue conditionResult = new PrimitiveValue(isResultTrue?"1":"0", true, variable);
		conditionResult.setVarID(varID);
		node.addWrittenVariable(conditionResult);
	}
	
	/**
	 * Generate the condition result variable name
	 * 
	 * The name follow the pattern: prefix + trace node order
	 * 
	 * @param order Trace node order of the condition
	 * @return Name of the condition result variable
	 */
	private String genConditionResultName(final int order) {
		return ProbabilityEncoder.CONDITION_RESULT_NAME_PRE + order;
	}
	
	/**
	 * Generate the condition result variable ID
	 * 
	 * The ID follow the pattern: prefix + trace node order
	 * 
	 * @param order Trace node order of the condition
	 * @return ID of the condition result variable
	 */
	private String genConditionResultID(final int order) {
		return ProbabilityEncoder.CONDITION_RESULT_ID_PRE + order;
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
	
	/**
	 * Initialize the correctness probability of variable and statement
	 */
//	private void preEncode() {
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
//		
//		for (TraceNode node : this.trace.getExecutionList()) {
//			node.setProbability(Configs.UNCERTAIN);
//			for (VarValue readVar : node.getReadVariables()) {
//				if (this.inputVars.contains(readVar)) {
//					readVar.setProbability(Configs.HIGH);
//				} else {
//					readVar.setProbability(Configs.UNCERTAIN);
//				}
//			}
//			for (VarValue writeVar : node.getWrittenVariables()) {
//				if (this.outputVars.contains(writeVar)) {
//					writeVar.setProbability(Configs.LOW);
//				} else {
//					writeVar.setProbability(Configs.UNCERTAIN);
//				}
//			}
//		}
//		for (TraceNode node : this.executionList) {
//			System.out.println("Init node: " + node.getOrder());
//			for (VarValue readVar : node.getReadVariables()) {
//				System.out.println("Init readVar : " + readVar.getVarName() + " with prob = " + readVar.getProbability());
//			}
//			for (VarValue writeVar : node.getWrittenVariables()) {
//				System.out.println("Init writeVar: " + writeVar.getVarName() + " with prob = " + writeVar.getProbability());
//			}
//		}
//	}
	
//	private void setProb(List<VarValue> vars, double prob) {
//		for (VarValue var : vars) {
//			if (var.getProbability() == 0) {
//				var.setProbability(prob);
//			}
//		}
//	}
	
//	private void printVarExhaustively(VarValue v, int n) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < n; i++) {
//			sb.append(' ');
//		}
//		sb.append(v.toString());
//		System.out.println(sb.toString());
//		for (VarValue child : v.getChildren()) {
//			printVarExhaustively(child, n + 2);
//		}
//	}
	
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
