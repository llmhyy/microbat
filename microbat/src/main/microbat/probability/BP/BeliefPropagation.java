package microbat.probability.BP;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.probability.BP.constraint.Constraint;
import microbat.util.TraceUtil;
import microbat.util.UniquePriorityQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import debuginfo.NodeFeedbackPair;


/**
 * Belief Propagation follow the procedure in ICSE18 paper
 * It will first calculate probability of correctness of variable using sum-product algorithm,
 * and then calculate of correctness of each statement by brute force
 * @author David, Siang Hwee 
 *
 */
public class BeliefPropagation {
	
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
	private List<TraceNode> executionList = null;
	
	/**
	 * True if the post-processing of the trace is done
	 */
	private boolean setupFlag = false;

	/**
	 * List of user feedback on correspondence node
	 */
	private static List<NodeFeedbackPair> userFeedbacks = new ArrayList<>();
	
	/**
	 * Output variables of the program, which assumed to be wrong
	 */
	private List<VarValue> outputVars = null;
	
	/**
	 * Input variables of the program, which assume to be correct
	 */
	private List<VarValue> inputVars = null;
	
	/**
	 * Constructor
	 * @param trace Trace of testing program
	 */
	public BeliefPropagation(Trace trace) {
		this.trace = trace;
	}
	
	/**
	 * Post process the given trace
	 * 1. Dynamic slicing based on the output
	 * 2. Remove this variable to reduce computation cost
	 * 3. Unify variable ID
	 * 4. Add condition result
	 * 5. Reset all the constraint id
	 * 
	 * Ensure that the input and output variables are
	 * set up properly before calling this function
	 */
	public void setup() {
		
		if (this.inputVars == null) {
			throw new RuntimeException("Fail to run Belief Propagation because the input variable is not set up");
		}
		
		if (this.outputVars == null) {
			throw new RuntimeException("Fail to run Belief Propagation because the output varaibles is not set up");
		}
		
		// Dynamic slicing based on output variables
		if (this.executionList == null) {
			this.executionList = TraceUtil.dyanmicSlice(this.trace, this.outputVars);
		}
		
		// Clear all previous feedbacks
		BeliefPropagation.clearFeedbacks();

		// Remove this variable
		this.removeThisVar(executionList);
		
		// Solve the problem of same variable ID after re-definition
		this.unifyArrayElementID(this.executionList);
//		this.unifyRefVarID(executionList);
//		this.changeRedefinitionID(this.executionList);
		
		// Add condition result as control predicate
//		this.addConditionResult(trace.getExecutionList());

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

		if (!setupFlag) {
			setup();
			return;
		}
		
		VariableEncoderFG varEncoder = new VariableEncoderFG(this.trace, this.executionList, this.inputVars, this.outputVars);
		
		// Include all the previous users feedback
		varEncoder.setFeedbacks(BeliefPropagation.userFeedbacks);
		
		long startTime = System.currentTimeMillis();
		
		// Calculate the probability for variables
		varEncoder.encode();	
		
		// Calculate the probability for statements
		new StatementEncoderFG(trace, executionList).encode();
		
		long endTime = System.currentTimeMillis();
		
		long executionTime = Math.floorDiv(endTime - startTime, 1000);
		System.out.println("Execution Time: " + executionTime + "s");
	}
	
	/**
	 * Add the users feedback 
	 * @param newFeedback Feedback of corresponding node
	 */
	public static void addFeedback(NodeFeedbackPair newFeedback) {
		// Check is the node already have feedback. If yes, update feedback
		boolean found = false;
		for (NodeFeedbackPair pair : BeliefPropagation.userFeedbacks) {
			if (pair.reviewingSameNode(newFeedback)) {
				pair.setFeedback(newFeedback.getFeedback());
				found = true;
				break;
			}
		}
		if (!found) {
			BeliefPropagation.userFeedbacks.add(newFeedback);
		}
	}
	
	/**
	 * Remove all previous feedbacks. The function is used when resetting the probability encoder
	 */
	public static void clearFeedbacks() {
		BeliefPropagation.userFeedbacks.clear();
	}
	
	/**
	 * Get the number of feedbacks used
	 * @return Number of feedbacks
	 */
	public static int getFeedbackCount() {
		return BeliefPropagation.userFeedbacks.size();
	}
	
	/**
	 * Access all the feedback of corresponding node
	 * @return List of node-feedback pair
	 */
	public static List<NodeFeedbackPair> getFeedbacks() {
		return BeliefPropagation.userFeedbacks;
	}
	
	/**
	 * Print the node probability for all executed node
	 */
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
	 * 
	 * It will ignore the last node in list, which is assumed to be
	 * error node.
	 * 
	 * @return Predicted wrong node
	 */
	public TraceNode getMostErroneousNode() {
		TraceNode errorNode = this.executionList.get(0);
		for (int i=1; i<this.executionList.size()-1; i++) {
			TraceNode node = this.executionList.get(i);
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
	
	/**
	 * Change all the array element id the same as their parent.
	 * 
	 * The purpose is to simplify the factor graph
	 * 
	 * @param executionList Execution list of testing program
	 */
	private void unifyArrayElementID(List<TraceNode> executionList) {
		Map<String, String> addressMap = new HashMap<>();
		for (TraceNode node : executionList) {
			List<VarValue> vars = new ArrayList<>();
			vars.addAll(node.getReadVariables());
			vars.addAll(node.getWrittenVariables());
			
			// Store the address (alias id) for every parent variable
			for (VarValue var : vars) {
				if (!var.getChildren().isEmpty() || addressMap.containsKey(var.getAliasVarID())) {
					addressMap.put(var.getAliasVarID(), var.getVarID());
				}
			}
			
			// If the children have the same address as their parent, then change the
			// children id the same as their parent
			for (VarValue var : vars) {
				if (!var.getParents().isEmpty()) {
					String address = this.extractAddressFromElementID(var.getVarID());
					if (address != null && addressMap.get(address) != null) {
						var.setVarID(addressMap.get(address));
					}
				}
			}
		}
	}
	
	private void unifyRefVarID(List<TraceNode> executionList) {
		Map<String, String> addressMap = new HashMap<>();
		for (TraceNode node : executionList) {
			List<VarValue> vars = new ArrayList<>();
			vars.addAll(node.getReadVariables());
			vars.addAll(node.getWrittenVariables());
			
			for (VarValue var : vars) {
				if (var instanceof ArrayValue) {
					continue;
				} else if (var instanceof ReferenceValue) {
					if (!addressMap.containsKey(var.getAliasVarID())) {
						addressMap.put(var.getAliasVarID(), var.getVarID());
					}
				}
			}
			
			for (VarValue var : vars) {
				if (var instanceof ArrayValue) {
					continue;
				} else if (var instanceof ReferenceValue) {
					if (addressMap.containsKey(var.getAliasVarID())) {
						var.setVarID(addressMap.get(var.getAliasVarID()));
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
	
	/**
	 * Remove this variable from the node.
	 * @param executionList Target execution list
	 */
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
	 * Perform dynamic slicing based on the outputs variables
	 * until it reach the inputs.
	 * 
	 * @param outputs Output variables for the testing program
	 * @return Execution list after dynamic slicing
	 */
	private List<TraceNode> dynamicSlicing(Trace trace, List<VarValue> outputs) {

		/*
		 * Get trace node that contain the output variable or input variables and store them into 
		 * toVisit Node list and destination list respectively
		 * 
		 * In case that there are multiple trace node that contain the same output variable,
		 * we only consider the last one. (Since the other will be included during dynamic
		 * slicing
		 * 
		 */

		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		
		List<VarValue> outputsCopy = new ArrayList<>();
		outputsCopy.addAll(outputs);
		
		// Search in reversed order because output usually appear at the end
		for (int order = trace.getLatestNode().getOrder(); order>=1; order--) {
			TraceNode node = trace.getTraceNode(order);
			
			// The throwing exception is not considered
			if (node.isThrowingException()) {
				continue;
			}
			
			// Store to visit node
			Iterator<VarValue> iter = outputsCopy.iterator();
			while(iter.hasNext()) {
				VarValue output = iter.next();
				if (node.isReadVariablesContains(output.getVarID()) ||
					node.isWrittenVariablesContains(output.getVarID())) {
					toVisitNodes.add(node);
					iter.remove();
				}
			}
			
			// Early stopping: when all output has been found
			if (outputsCopy.isEmpty()) {
				break;
			}
		}
		
		// Perform dynamic slicing base starting from output trace node
		Set<TraceNode> slicingSet = new HashSet<>();
		while (!toVisitNodes.isEmpty()) {
			TraceNode node = toVisitNodes.poll();
			
			// Add data dominator
			for (VarValue readVar : node.getReadVariables()) {
				TraceNode dataDom = trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					toVisitNodes.add(dataDom);
				}
			}
			
			// Add control dominator
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				toVisitNodes.add(controlDom);
			}
			
			slicingSet.add(node);
		}
		
		// Sort the result
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
		return BeliefPropagation.CONDITION_RESULT_NAME_PRE + order;
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
		return BeliefPropagation.CONDITION_RESULT_ID_PRE + order;
	}
}
