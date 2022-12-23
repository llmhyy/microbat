package microbat.probability.SPP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	
	/**
	 * Correctness Propagation Factor
	 */
	private double alpha = 1.0;
	
	/**
	 * Wrongness Propagation Factor
	 */
	private double beta = 1.0;
	
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
	 * Propagate the correctness probability
	 * in one direction
	 */
	public void propagate() {
		
		if (!this.isReady()) {
			return;
		}
		
		this.init();
		
		for (TraceNode node : this.slicedTrace) {
			double writeProb = this.forwardProp(node);
			if (writeProb != -1.0) {
				for (VarValue writeVar : node.getWrittenVariables()) {
					// Do not overwrite the input
					if (this.inputs.contains(writeVar)) {
						continue;
					}
					writeVar.setProbability(writeProb);
				}
			}
		}
		
		for (TraceNode node : this.slicedTrace) {
			if (!node.isBranch()) {
				continue;
			}
			
			VarValue controlDom = node.getConditionResult();
			if (this.inputs.contains(controlDom) || this.outputs.contains(controlDom)) {
				continue;
			}
			
			List<VarValue> writtenVars = new ArrayList<>();
			for (TraceNode controlDominatee : node.getControlDominatees()) {
				writtenVars.addAll(controlDominatee.getWrittenVariables());
			}
			
			double prob = this.aggregator.aggregate(writtenVars, ProbAggregateMethods.AVG);
			if (prob != ProbAggregator.NON_PROB) {
				controlDom.setProbability(prob);
			}
		}
	}
	
	public void backPropagate() {
		
		// Initialize the probability
		this.init();
		
		// Calculate computational cost
		this.computeComputationalCost();
		
//		this.buildInputRelation();
		
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
			double avgProb = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.AVG);
			
			// Calculate maximum gain
			VarValue writtenVar = node.getWrittenVariables().get(0);
			long cumulativeCost = writtenVar.getComputationalCost();
			long opCost = this.countModifyOperation(node);
			double gain = 0;
			if (cumulativeCost != 0) {
				gain = (0.95 - avgProb) * (opCost/cumulativeCost);
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
					readVar.setProbability(PropProbability.HIGH);
					continue;
				}
				
				double factor = 1;
				if (totalCost != 0) {
					if (readVar.getComputationalCost() != totalCost) {
						factor = 1 - readVar.getComputationalCost() / (double) totalCost;
					}
				}
				
				double prob = avgProb + gain  * factor;
				readVar.setProbability(prob);
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
			
			/*
			 * Written variables' computational cost is calculated as
			 * sum of cost of read variable plus one.
			 */
			long cumulatedCost = 0;
			for (VarValue readVar : node.getReadVariables()) {
				cumulatedCost += readVar.getComputationalCost();
			}
			
			long cost = this.countModifyOperation(node);
			
			for (VarValue writtenVar : node.getWrittenVariables()) {
				writtenVar.setComputationalCost(cumulatedCost+cost);
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
			double avgProb = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.AVG);
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
		double writtenProb = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.MIN);
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
	
	/**
	 * Propagation the correctness probability
	 * in two direction
	 */
	public void propagate_biDir() {
		
		if (!this.isReady()) {
			return;
		}
		
		this.init();
		
		int forward_ptr = 0;
		int backward_ptr = this.slicedTrace.size()-1;
		
		while (forward_ptr < backward_ptr) {
			// Correctness propagation
			TraceNode forwardNode = this.slicedTrace.get(forward_ptr);
			double writeProb = this.forwardProp(forwardNode);
			if (writeProb != -1.0) {
				for (VarValue writeVar : forwardNode.getWrittenVariables()) {
					
					// Do not overwrite the input
					if (this.inputs.contains(writeVar)) {
						continue;
					}
					
					writeVar.setProbability(writeProb < 0.5 ? 0.5 : writeProb);
				}
			}

			// Wrongness propagation
			TraceNode backwardNode = this.slicedTrace.get(backward_ptr);
			double readProb = this.backwardProp(backwardNode);
			if (readProb != 2.0) {
				for (VarValue readVar : backwardNode.getReadVariables()) {
					
					// Do not overwrite the output
					if (this.outputs.contains(readVar)) {
						continue;
					}
					
					readVar.setProbability(readProb > 0.5 ? 0.5 : readProb);
				}
			}
			
			forward_ptr += 1;
			backward_ptr -= 1;
		}
		
		TraceNode middleNode = this.slicedTrace.get(forward_ptr);
		if (forward_ptr == backward_ptr) {
			this.passForwardProp(middleNode);
			this.passBackwardProp(middleNode);
		} else if (forward_ptr > backward_ptr) {
			this.passForwardProp(middleNode);
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
				double prob = this.aggregator.aggregate(node.getReadVariables(), ProbAggregateMethods.AVG);
				drop = 1 - prob;
			} else if (node.getReadVariables().isEmpty()) {
				// Case 4
				double prob = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.AVG);
				drop = 1 - prob;
			} else {
				double readProb = this.aggregator.aggregate(node.getReadVariables(), ProbAggregateMethods.MIN);
				double writtenProb = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.MIN);
				drop = readProb - writtenProb;
			}
			
			if (drop < 0) {
				// Case that the read variable is wrong but the written variable is correct
				// Ignore it by now
				
				System.out.println("Warning: Trace node " + node.getOrder() + " has negative drop");
				continue;
			} else {
				if (drop > maxDrop) {
					maxDrop = drop;
					rootCause = node;
				}
			}
		}
		return rootCause;
	}
	
	/**
	 * Propagate the correctness probability
	 * from read variables to written variables
	 * @param node Current trace node
	 * @return Propagated probability
	 */
	private double forwardProp(final TraceNode node) {
		this.passForwardProp(node);
		
		double prob = this.aggregator.aggregate(node.getReadVariables(), ProbAggregateMethods.AVG);
		if (prob == -1.0) {
			return -1.0;
		} else {
			return this.alpha * prob;
		}
	}
	
	/**
	 * Propagate the wrongness probability
	 * from written variables to written variables
	 * @param node Current trace node
	 * @return Propagated probability
	 */
	private double backwardProp(final TraceNode node) {
		this.passBackwardProp(node);
		
		double prob = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.AVG);

		if (prob == 2.0) {
			return 2.0;
		} else {
			return this.beta * prob;
		}
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
				readVar.setProbability(PropProbability.HIGH);
				continue;
			}
			
			VarValue dataDomVar = this.findDataDomVar(readVar, node);
			if (dataDomVar != null) {
				readVar.setProbability(dataDomVar.getProbability());
			}
		}
	}
	
	private VarValue findDataDomVar(final VarValue var, final TraceNode node) {
		final String varID = Variable.truncateSimpleID(var.getVarID());
		final String headID = Variable.truncateSimpleID(var.getAliasVarID());
		
		TraceNode dataDominator = this.trace.findDataDependency(node, var);
		if (dataDominator != null) {
			for (VarValue writeVar : dataDominator.getWrittenVariables()) {
				if (writeVar.equals(var)) {
					return writeVar;
				}
//				final String wVarID = Variable.truncateSimpleID(writeVar.getVarID());
//				final String wHeadID = Variable.truncateSimpleID(writeVar.getAliasVarID());
//				
//				if(wVarID != null && wVarID.equals(varID)) {
//					return writeVar;	
//				} else if (wHeadID != null && wHeadID.equals(headID)) {
//					return writeVar;
//				} else {
//					VarValue childValue = writeVar.findVarValue(varID, headID);
//					if(childValue != null) {
//						return writeVar;
//					}
//				}
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
				writeVar.setProbability(PropProbability.LOW);
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
				writeVar.setProbability(PropProbability.UNCERTAIN);
			} else {
				double maxProb = -1.0;
				for (TraceNode dataDominate : dataDominatees) {
					for (VarValue readVar : dataDominate.getReadVariables()) {
						if (readVar.equals(writeVar)) {
							final double prob = readVar.getProbability();
							maxProb = Math.max(prob, maxProb);
						}
					}
				}
				writeVar.setProbability(maxProb);
			}
		}
		
		if (node.isBranch()) {
			VarValue conditionResult = node.getConditionResult();
			
			if (this.inputs.contains(conditionResult)) {
				conditionResult.setProbability(PropProbability.HIGH);
			} else if (this.outputs.contains(conditionResult)) {
				conditionResult.setProbability(PropProbability.LOW);
			} else {
				double avgProb = 0.0;
				int count = 0;
				for (TraceNode controlDominatee : node.getControlDominatees()) {
					if (!this.slicedTrace.contains(controlDominatee)) {
						continue;
					}
					for (VarValue writtenVar : controlDominatee.getWrittenVariables()) {
						avgProb += writtenVar.getProbability();
						count += 1;
					}
				}
				avgProb = count == 0 ? PropProbability.UNCERTAIN : avgProb/count;
				conditionResult.setProbability(avgProb);
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
	private void init() {
		
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setProbability(PropProbability.UNCERTAIN);
				if (this.inputs.contains(readVar)) {
					readVar.setProbability(PropProbability.HIGH);
				}
				if (this.outputs.contains(readVar)) {
					readVar.setProbability(PropProbability.LOW);
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				writeVar.setProbability(PropProbability.UNCERTAIN);
				if (this.inputs.contains(writeVar)) {
					writeVar.setProbability(PropProbability.HIGH);
				}
				if (this.outputs.contains(writeVar)) {
					writeVar.setProbability(PropProbability.LOW);
				}
			}
		}

//		this.alpha = this.calAlpha(this.slicedTrace.size());
//		this.beta = this.calBeta(this.slicedTrace.size());
//		this.addConditionResult(this.slicedTrace);
	}
	
	private double calAlpha(final int traceLen) {
		double alpha = PropProbability.LOW / PropProbability.HIGH;
		alpha = Math.pow(alpha, (double) 1/traceLen);
		return alpha;
	}
	
	private double calBeta(final int traceLen) {
		double beta= PropProbability.HIGH / PropProbability.LOW;
		beta = Math.pow(beta, (double) 1/traceLen);
		return beta;
	}
	
	private boolean isReady() {
		if (this.inputs.isEmpty() || this.outputs.isEmpty()) {
			throw new RuntimeException("StepwisePropagator: Please provide inputs and outputs variables");
		}
		return true;
	}
	
	public void responseToFeedback(NodeFeedbackPair nodeFeedbackPair) {
		TraceNode node = nodeFeedbackPair.getNode();
		
		UserFeedback feedback = nodeFeedbackPair.getFeedback();
		
		if (feedback.getFeedbackType() == UserFeedback.CORRECT) {
			this.addInputs(node.getReadVariables());
			this.addInputs(node.getWrittenVariables());
			TraceNode controlDominator = node.getControlDominator();
			if (controlDominator != null) {
				VarValue controlDom = controlDominator.getConditionResult();
				this.inputs.add(controlDom);
			}
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
			TraceNode controlDominator = node.getControlDominator();
			VarValue controlDom = controlDominator.getConditionResult();
			this.outputs.add(controlDom);
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
			VarValue wrongVar = feedback.getOption().getReadVar();
			this.outputs.add(wrongVar);
//			for (VarValue readVar : node.getReadVariables()) {
//				if (!readVar.equals(wrongVar)) {
//					this.inputs.add(readVar);
//				}
//			}
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
