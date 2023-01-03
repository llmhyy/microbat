package microbat.probability.SPP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import debuginfo.NodeFeedbackPair;
import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.probability.PropProbability;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

/**
 * StepwisePropagator is used to propagate
 * the probability of correctness
 * 
 * It propagate the probability by only
 * considering the current node and it's
 * first order neighbors
 * 
 * Time complexity is linear. O(n) where n
 * is the trace length
 * 
 * @author David
 *
 */
public class StepwisePropagator {
	
	/**
	 * Execution trace of target program
	 */
	private Trace trace;
	
	/**
	 * List of input variables which assumed to be correct
	 */
	private List<VarValue> inputs = new ArrayList<>();
	
	/**
	 * List of outputs variables which assumed to be wrong
	 */
	private List<VarValue> outputs = new ArrayList<>();
	
	/**
	 * List of executed trace node after dynamic slicing
	 */
	private List<TraceNode> slicedTrace = null;
	
	private ProbAggregator aggregator = new ProbAggregator();
	
	private List<NodeFeedbackPair> feedbackRecords = new ArrayList<>();
	
	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	
	/*
	 *  Correctness threshold
	 */
	private double correctThd = 0.7;
	
	/*
	 * 	Wrongness threshold
	 */
	private double wrongThd = 0.3;
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 */
	public StepwisePropagator(Trace trace) {
		this.trace = trace;
		this.aggregator = new ProbAggregator();
	}
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 * @param inputs Input variables which assumed to be correct
	 * @param outputs Output variables which assumed to be wrong
	 */
	public StepwisePropagator(Trace trace, List<VarValue> inputs, List<VarValue> outputs) {
		this.trace = trace;
		this.inputs = inputs;
		this.outputs = outputs;
		this.aggregator = new ProbAggregator();
		this.slicedTrace = TraceUtil.dyanmicSlice(trace, this.outputs);
		this.constructUnmodifiedOpcodeType();
	}
	
	/**
	 * Add input variables
	 * @param inputs Input variables
	 */
	public void addInputs(Collection<VarValue> inputs) {
		this.inputs.addAll(inputs);
	}
	
	/**
	 * Add output variables
	 * @param outputs Output variables
	 */
	public void addOutputs(Collection<VarValue> outputs) {
		this.outputs.addAll(outputs);
	}

	/**
	 * Combine the forward probability and backward probability.
	 * 
	 * By now, we directly copy the backward probability
	 */
	public void combineProbability() {
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setProbability(readVar.getBackwardProb());
			}
			for (VarValue writtenVar : node.getWrittenVariables()) {
				writtenVar.setProbability(writtenVar.getBackwardProb());
			}
		}
	}
	
	public void backwardPropagate() {
		
		// Loop the execution list backward
		for (int order = this.slicedTrace.size()-1; order>=0; order--) {
			TraceNode node = this.slicedTrace.get(order);
			
			// Skip this node if the feedback is already given
			if (this.isFeedbackGiven(node)) {
				System.out.println("TraceNode: " + node.getOrder() + " is skipped because feedback is already given");
				continue;
			}
			
			// Initialize written variables probability
			this.passBackwardProp(node);
			
			// Skip when there are no read variables
			if (node.getReadVariables().isEmpty() || node.getWrittenVariables().isEmpty()) {
				System.out.println("TraceNode: " + node.getOrder() + " is skipped because there are no either read or written variable");
				continue;
			}
			
			// Aggregate written variable probability
			double avgProb = 0.0;
			for (VarValue writtenVar : node.getWrittenVariables()) {
				avgProb += writtenVar.getBackwardProb();
			}
			avgProb /= node.getWrittenVariables().size();
			
			// Calculate maximum gain
			VarValue writtenVar = node.getWrittenVariables().get(0);
			long cumulativeCost = writtenVar.getComputationalCost();
			long opCost = this.countModifyOperation(node);
			double gain = 0;
			if (cumulativeCost != 0) {
				gain = (0.95 - avgProb) * ((double) opCost/cumulativeCost);
			}

			// Calculate total cost
			int totalCost = 0;
			for (VarValue readVar : node.getReadVariables()) {
				totalCost += readVar.getComputationalCost();
			}

			for (VarValue readVar : node.getReadVariables()) {
				
				// Ignore this variable if it is input or output
				if (this.outputs.contains(readVar) || this.inputs.contains(readVar)) {
					continue;
				}
				
				if (readVar.isThisVariable()) {
					readVar.setBackwardProb(PropProbability.HIGH);
					continue;
				}
				
				double factor = 1;
				if (totalCost != 0) {
					if (readVar.getComputationalCost() != totalCost) {
						factor = 1 - readVar.getComputationalCost() / (double) totalCost;
					}
				}
				
				double prob = avgProb + gain  * factor;
				readVar.setBackwardProb(prob);
			}
		}
	}
	
	/**
	 * Calculate the computation cost of
	 * each variables
	 */
	public void computeComputationalCost() {
		for (TraceNode node : this.slicedTrace) {
			
			// Inherit the computation cost from data dominator
			for (VarValue readVar : node.getReadVariables()) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.setComputationalCost(dataDomVar.getComputationalCost());
				}
			}
			
			// Sum of read variables computational cost
			long cumulatedCost = 0;
			for (VarValue readVar : node.getReadVariables()) {
				cumulatedCost += readVar.getComputationalCost();
			}
			
			// Operational cost
			long opCost = this.countModifyOperation(node);
			
			// Define written variables computational cost
			for (VarValue writtenVar : node.getWrittenVariables()) {
				long cost = cumulatedCost + opCost;
				writtenVar.setComputationalCost(cost);
			}
		}
	}
	
	private void buildInputRelation() {
		for (TraceNode node : this.slicedTrace) {
			boolean readVarRelatedToInput = false;
			for (VarValue readVar : node.getReadVariables()) {
				if (this.inputs.contains(readVar)) {
					readVar.isInputRelated(true);
					readVarRelatedToInput = true;
					continue;
				}
				
				VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar == null) {
					readVar.isInputRelated(false);
				} else {
					readVar.isInputRelated(dataDomVar.isInputRelated());
				}
				
				if (readVar.isInputRelated()) {
					readVarRelatedToInput = true;
				}
			}
			
			for (VarValue writtenVar : node.getWrittenVariables()) {
				if (this.inputs.contains(writtenVar)) {
					writtenVar.isInputRelated(true);
					continue;
				}
				writtenVar.isInputRelated(readVarRelatedToInput);
			}
		}
		
		for (TraceNode node : this.slicedTrace) {
			System.out.println("--------------------------");
			System.out.println("TraceNode: " + node.getOrder());
			for(VarValue readVar : node.getReadVariables()) {
				System.out.println("ReadVar: " + readVar.getVarID() + " related to input: " + readVar.isInputRelated());
			}
			for (VarValue writtenVar : node.getWrittenVariables()) {
				System.out.println("WrittenVar: " + writtenVar.getVarID() + " related to input: " + writtenVar.isInputRelated());
			}
			System.out.println();
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
	
	public List<NodeFeedbackPair> findPathway(final TraceNode startingNode, final TraceNode destinationNode) {
		
		Stack<NodeFeedbackPair> path = new Stack<>();
		
		TraceNode currentNode = startingNode;
		TraceNode prevNode = null;
		while (!currentNode.equals(destinationNode)) {
			
			if (currentNode.getOrder()<destinationNode.getOrder()) {
				break;
			}
			
			TraceNode controlDom = currentNode.getControlDominator();
			if (controlDom != null) {
				
				if (controlDom.getOrder() < destinationNode.getOrder()) {
					
					// Will not give control slicing feedback if the control dom is beyond the destination node
				} else {
					VarValue controlDomVar = controlDom.getConditionResult();
					if (controlDomVar.getProbability() < this.wrongThd) {
						UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
						NodeFeedbackPair pair = new NodeFeedbackPair(currentNode, feedback);
						path.add(pair);
						
						currentNode = controlDom;
						continue;
					}
				}
				
			}
			
			if (currentNode.getReadVariables().isEmpty()) {
				break;
			}
			
			VarValue mostSubVar = this.getMostSupReadVar(currentNode);
			ChosenVariableOption option = new ChosenVariableOption(mostSubVar, null);
			UserFeedback feedback = new UserFeedback(option, UserFeedback.WRONG_VARIABLE_VALUE);
			NodeFeedbackPair pair = new NodeFeedbackPair(currentNode, feedback);
			path.add(pair);
			
			prevNode = currentNode;
			currentNode = this.trace.findDataDependency(currentNode, mostSubVar);
			
			if (currentNode == null) {
				UserFeedback unclearFeedback = new UserFeedback(UserFeedback.UNCLEAR);
				NodeFeedbackPair endPair = new NodeFeedbackPair(prevNode, unclearFeedback);
				path.add(endPair);
				break;
			}
		}
		
		if (currentNode == null) {
			return path;
		}
		
		if (currentNode.equals(destinationNode)) {
			UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
			NodeFeedbackPair pair = new NodeFeedbackPair(currentNode, feedback);
			path.add(pair);
		}
		
		return path;
	}
	
	private void findPathway_(final TraceNode startingNode, final TraceNode destinationNode, Stack<NodeFeedbackPair> path) {
		
	}
	
	public UserFeedback giveFeedback(final TraceNode node) {
		UserFeedback feedback = new UserFeedback();
		
		// Check control correctness
		if (node.getControlDominator() != null) {
			TraceNode controlDom = node.getControlDominator();
			VarValue controlDomVar = controlDom.getConditionResult();
			if (controlDomVar.getProbability() < this.wrongThd) {
				feedback.setFeedbackType(UserFeedback.WRONG_PATH);
				return feedback;
			}
		}
		
		// Handle the case that there are no read and written variables
		if (node.getWrittenVariables().isEmpty() && node.getReadVariables().isEmpty()) {
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			return feedback;
		}
		
		// Handle the case that there are no read variables
		if (node.getReadVariables().isEmpty()) {
			double avgProb = this.aggregator.aggregateProb(node.getWrittenVariables(), ProbAggregateMethods.AVG);
			if (avgProb < this.wrongThd) {
				feedback.setFeedbackType(UserFeedback.ROOTCAUSE);
			} else {
				feedback.setFeedbackType(UserFeedback.UNCLEAR);
			}
			return feedback;
		}
		
		// Handle the case that there are no written variables
		if (node.getWrittenVariables().isEmpty()) {
			VarValue wrongVar = this.getMostSupReadVar(node);
			if (wrongVar.getProbability() < this.wrongThd) {
				feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
				feedback.setOption(new ChosenVariableOption(wrongVar, null));
			} else {
				feedback.setFeedbackType(UserFeedback.UNCLEAR);
			}
			return feedback;
		}
		
		// Check is the node root cause or not
		double writtenProb = this.aggregator.aggregateProb(node.getWrittenVariables(), ProbAggregateMethods.MIN);
		VarValue supVar = this.getMostSupReadVar(node);
		double readProb = supVar.getProbability();
		
		if (writtenProb > this.correctThd) {
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			return feedback;
		}
		
		if (readProb > this.correctThd) {
			feedback.setFeedbackType(UserFeedback.ROOTCAUSE);
			return feedback;
		} else {
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(supVar, null));
			return feedback;
		}
	}
	
	private VarValue getMostSupReadVar(final TraceNode node) {
		if (node.getReadVariables().isEmpty()) {
			throw new IllegalArgumentException("StepwisePropagator: getMostSupReadVar but there are no read variables");
		}
		
		double minProb = 2.0;
		VarValue supVar = null;
		
		for (VarValue readVar : node.getReadVariables()) {
			double prob = readVar.getProbability();
			if (prob < minProb) {
				minProb = prob;
				supVar = readVar;
			}
		}
		
		return supVar;
	}
	
	
	public void forwardPropagation() {
		for (TraceNode node : this.slicedTrace) {
			if (node.getReadVariables().isEmpty() || node.getWrittenVariables().isEmpty()) {
				continue;
			}
			
			this.passForwardProp(node);
			
			// Deep copy the array list
			List<VarValue> readVars = new ArrayList<>();
			readVars.addAll(node.getReadVariables());
			
			// Ignore this variable
			readVars.removeIf(element -> (element.isThisVariable()));
			
			if (readVars.isEmpty()) {
				continue;
			}
			
			double avgProb = this.aggregator.aggregateForwardProb(readVars, ProbAggregateMethods.AVG);
//			double avgProb = 0.0;
//			for (VarValue readVar : readVars) {
//				avgProb += readVar.getForwardProb();
//			}
//			avgProb /= readVars.size();
			
			if (avgProb <= PropProbability.UNCERTAIN) {
				continue;
			} else {
				long minCost = this.outputs.get(0).getComputationalCost();
				for (int i=1; i<this.outputs.size(); i++) {
					VarValue output = this.outputs.get(i);
					final long cost = output.getComputationalCost();
					if (cost < minCost) {
						minCost = cost;
					}
				}
				
				long writtenCost = node.getWrittenVariables().get(0).getComputationalCost();
				double loss = (avgProb - 0.5) * ((double) writtenCost / minCost);
				double prob = avgProb - loss;
				
				for (VarValue writtenVar : node.getWrittenVariables()) {
					if (this.inputs.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.HIGH);
					} else if (this.outputs.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.LOW);
					} else {
						writtenVar.setForwardProb(prob);
					}
				}
				
			}
		}
	}
	
	
	/**
	 * Propose the root cause node. <br><br>
	 * 
	 * It will compare the drop of correctness
	 * probability from read variables to the
	 * written variables. <br><br>
	 * 
	 * The one with the maximum drop will be the
	 * root cause. <br><br>
	 * 
	 * @return Root cause node
	 */
	public TraceNode proposeRootCause() {
		TraceNode rootCause = null;
		double maxDrop = 0.0;
		for (TraceNode node : this.slicedTrace) {
			
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			/*
			 * We need to handle:
			 * 1. Node without any variable
			 * 2. Node with only control dominator
			 * 3. Node with only read variables
			 * 4. Node with only written variables
			 * 5. Node with both read and written variables
			 * 
			 * It will ignore the node that already have feedback
			 */
			double drop = 0.0;
			if (node.getWrittenVariables().isEmpty() && node.getReadVariables().isEmpty() && node.getControlDominator() == null) {
				// Case 1
				continue;
			} else if (node.getWrittenVariables().isEmpty() && node.getReadVariables().isEmpty() && node.getControlDominator() != null) {
				// Case 2
				drop = PropProbability.UNCERTAIN - node.getControlDominator().getWrittenVariables().get(0).getProbability();
			} else if (node.getWrittenVariables().isEmpty()) {
				// Case 3
				double prob = this.aggregator.aggregateProb(node.getReadVariables(), ProbAggregateMethods.AVG);
				drop = PropProbability.HIGH - prob;
			} else if (node.getReadVariables().isEmpty()) {
				// Case 4
				double prob = this.aggregator.aggregateProb(node.getWrittenVariables(), ProbAggregateMethods.AVG);
				drop = PropProbability.HIGH - prob;
			} else {
				double readProb = this.aggregator.aggregateForwardProb(node.getReadVariables(), ProbAggregateMethods.MIN);
				double writtenProb = this.aggregator.aggregateProb(node.getWrittenVariables(), ProbAggregateMethods.MIN);
				drop = readProb - writtenProb;
			}
			
			if (drop < 0) {
				// Case that the read variable is wrong but the written variable is correct
				// Ignore it by now
				
				System.out.println("Warning: Trace node " + node.getOrder() + " has negative drop");
				continue;
			} else {
				if (drop >= maxDrop) {
					maxDrop = drop;
					rootCause = node;
				}
			}
		}
		return rootCause;
	}
	
	/**
	 * Copy the correctness probability from
	 * data dominator of the read variables in
	 * given node. <br><br>
	 * 
	 * The input variable will be directly set to HIGH.
	 * 
	 * @param node Target trace node
	 */
	private void passForwardProp(final TraceNode node) {
		// Receive the correctness propagation
		for (VarValue readVar : node.getReadVariables()) {
			
			// Ignore the input variables such that it will not be overwritten
			if (this.inputs.contains(readVar)) {
				readVar.setAllProbability(PropProbability.HIGH);
				continue;
			}
			
			VarValue dataDomVar = this.findDataDomVar(readVar, node);
			if (dataDomVar != null) {
				readVar.setForwardProb(dataDomVar.getForwardProb());
			}
		}
	}
	
	private VarValue findDataDomVar(final VarValue var, final TraceNode node) {
		TraceNode dataDominator = this.trace.findDataDependency(node, var);
		if (dataDominator != null) {
			for (VarValue writeVar : dataDominator.getWrittenVariables()) {
				if (writeVar.equals(var)) {
					return writeVar;
				}
			}
		}
		return null;
	}
	
	/**
	 * Copy the correctness probability from
	 * data dominatees of the written variables in
	 * given node. <br><br>
	 * 
	 * If there are multiple data dominatees, then it will
	 * choose the maximum one. <br><br>
	 * 
	 * The output variable will be directly set to LOW.
	 * 
	 * Set probability into UNCLEAR if no data dominatees is found
	 * 
	 * @param node Target trace node
	 */
	private void passBackwardProp(final TraceNode node) {
		
		// Receive the wrongness propagation
		for (VarValue writeVar : node.getWrittenVariables()) {
			
			// Ignore the output variable such that it will not be overwritten
			if (this.outputs.contains(writeVar)) {
				writeVar.setAllProbability(PropProbability.LOW);
				continue;
			}
			
			List<TraceNode> dataDominatees = this.trace.findDataDependentee(node, writeVar);
			
			// Remove the node that does not contribute to the result
			for (int i=0; i<dataDominatees.size(); i++) {
				TraceNode dataDominatee = dataDominatees.get(i);
				if (!this.slicedTrace.contains(dataDominatee)) {
					dataDominatees.remove(i);
					i -= 1;
				}
			}
			
			// Do nothing if no data dominatees is found
			if (dataDominatees.isEmpty()) {
				writeVar.setBackwardProb(PropProbability.UNCERTAIN);
			} else {
				double maxProb = -1.0;
				for (TraceNode dataDominate : dataDominatees) {
					for (VarValue readVar : dataDominate.getReadVariables()) {
						if (readVar.equals(writeVar)) {
							final double prob = readVar.getBackwardProb();
							maxProb = Math.max(prob, maxProb);
						}
					}
				}
				writeVar.setBackwardProb(maxProb);
			}
		}
		
		if (node.isBranch()) {
			VarValue conditionResult = node.getConditionResult();
			
			if (this.inputs.contains(conditionResult)) {
				conditionResult.setAllProbability(PropProbability.HIGH);
			} else if (this.outputs.contains(conditionResult)) {
				conditionResult.setAllProbability(PropProbability.LOW);
			} else {
				double avgProb = 0.0;
				int count = 0;
				for (TraceNode controlDominatee : node.getControlDominatees()) {
					if (!this.slicedTrace.contains(controlDominatee)) {
						continue;
					}
					for (VarValue writtenVar : controlDominatee.getWrittenVariables()) {
						avgProb += writtenVar.getBackwardProb();
						count += 1;
					}
				}
				avgProb = count == 0 ? PropProbability.UNCERTAIN : avgProb/count;
				conditionResult.setBackwardProb(avgProb);
			}
		}
	}
	
	/**
	 * Initialize the probability of each variables
	 * 
	 * Inputs are set to 0.95. <br>
	 * Outputs are set to 0.05. <br>
	 * Others are set to 0.5.
	 */
	public void init() {
		
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setAllProbability(PropProbability.UNCERTAIN);
				if (this.inputs.contains(readVar)) {
					readVar.setAllProbability(PropProbability.HIGH);
				}
				if (this.outputs.contains(readVar)) {
					readVar.setAllProbability(PropProbability.LOW);
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				writeVar.setAllProbability(PropProbability.UNCERTAIN);
				if (this.inputs.contains(writeVar)) {
					writeVar.setAllProbability(PropProbability.HIGH);
				}
				if (this.outputs.contains(writeVar)) {
					writeVar.setAllProbability(PropProbability.LOW);
				}
			}
		}
	}
	
	/**
	 * Set probability based on the user feedbacks
	 * @param nodeFeedbackPairs List of user feedbacks
	 */
	public void responseToFeedbacks(Collection<NodeFeedbackPair> nodeFeedbackPairs) {
		for (NodeFeedbackPair pair : nodeFeedbackPairs) {
			this.responseToFeedback(pair);
		}
	}
	
	/**
	 * Set probability based on the user feedback
	 * @param nodeFeedbackPair User feedback 
	 */
	public void responseToFeedback(final NodeFeedbackPair nodeFeedbackPair) {
		TraceNode node = nodeFeedbackPair.getNode();
		UserFeedback feedback = nodeFeedbackPair.getFeedback();
		
		if (feedback.getFeedbackType() == UserFeedback.CORRECT) {
			// If the feedback is CORRECT, then set every variable and control dom to be correct
			this.addInputs(node.getReadVariables());
			this.addInputs(node.getWrittenVariables());
			TraceNode controlDominator = node.getControlDominator();
			if (controlDominator != null) {
				VarValue controlDom = controlDominator.getConditionResult();
				this.inputs.add(controlDom);
			}
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
			// If the feedback is WRONG_PATH, set control dominator varvalue to wrong
			TraceNode controlDominator = node.getControlDominator();
			VarValue controlDom = controlDominator.getConditionResult();
			this.outputs.add(controlDom);
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
			// If the feedback is WRONG_VARIABLE_VALUE, set that variable to be wrong
			// and set control dominator to be correct
			VarValue wrongVar = feedback.getOption().getReadVar();
			this.outputs.add(wrongVar);
			this.addOutputs(node.getWrittenVariables());
			
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				this.inputs.add(controlDom.getConditionResult());
			}
		}
		
		this.recordFeedback(nodeFeedbackPair);
	}
	
	private void recordFeedback(final NodeFeedbackPair pair) {
		this.feedbackRecords.add(pair);
	}
	
	private boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbackPair pair : this.feedbackRecords) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
	}
	
	private void constructUnmodifiedOpcodeType() {
		this.unmodifiedType.add(OpcodeType.LOAD_CONSTANT);
		this.unmodifiedType.add(OpcodeType.LOAD_FROM_ARRAY);
		this.unmodifiedType.add(OpcodeType.LOAD_VARIABLE);
		this.unmodifiedType.add(OpcodeType.STORE_INTO_ARRAY);
		this.unmodifiedType.add(OpcodeType.STORE_VARIABLE);
		this.unmodifiedType.add(OpcodeType.RETURN);
	}
}
