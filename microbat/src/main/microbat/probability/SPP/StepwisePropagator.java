package microbat.probability.SPP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import debuginfo.NodeFeedbackPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.probability.PropProbability;
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
	
	private ProbAggregator aggregator;
	
	/**
	 * Correctness Propagation Factor
	 */
	private double alpha = 1.0;
	
	/**
	 * Wrongness Propagation Factor
	 */
	private double beta = 1.0;
	
	private List<NodeFeedbackPair> feedbackRecords = new ArrayList<>();
	
	
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
		this.init();
		this.computeComputationalCost();
		for (int order = this.slicedTrace.size()-1; order>=0; order--) {
			TraceNode node = this.slicedTrace.get(order);
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			this.passBackwardProp(node);
			double avgProb = this.aggregator.aggregate(node.getWrittenVariables(), ProbAggregateMethods.AVG);
			
//			long minCost = this.slicedTrace.size()+1;
//			for (VarValue readVar : node.getReadVariables()) {
//				long cost = readVar.getComputationalCost();
//				if (cost == 0) {
//					continue;
//				}
//				if (readVar.getComputationalCost() < minCost) {
//					minCost = readVar.getComputationalCost();
//				}
//			}
//			if (minCost == this.slicedTrace.size()+1) {
//				minCost = node.getWrittenVariables().get(0).getComputationalCost();
//			}
			
			double gain = (0.95 - avgProb) / node.getWrittenVariables().get(0).getComputationalCost();
			int totalCost = 0;
			for (VarValue readVar : node.getReadVariables()) {
				totalCost += readVar.getComputationalCost();
			}
			for (VarValue readVar : node.getReadVariables()) {
				if (this.outputs.contains(readVar) || this.inputs.contains(readVar)) {
					continue;
				}
				double factor = 1;
				if (totalCost != 0) {
					factor = 1- readVar.getComputationalCost() / (double) totalCost;
				}
				
				if (factor == 0) {
					factor = 1;
				}
				readVar.setProbability(avgProb + gain * factor);
			}
		}
	}
	
	public void computeComputationalCost() {
		for (TraceNode node : this.slicedTrace) {
			
			for (VarValue readVar : node.getReadVariables()) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.setComputationalCost(dataDomVar.getComputationalCost());
				}
			}
			
			long cost = 0;
			for (VarValue readVar : node.getReadVariables()) {
				cost += readVar.getComputationalCost();
			}
				
			
			for (VarValue writtenVar : node.getWrittenVariables()) {
				writtenVar.setComputationalCost(cost+1);
			}
		}
		
		for (TraceNode node : this.slicedTrace) {
			System.out.println("TraceNode: " + node.getOrder());
			for (VarValue var : node.getReadVariables()) {
				System.out.println(var.getVarName() + " with cost: " + var.getComputationalCost());
			}
			for (VarValue var : node.getWrittenVariables()) {
				System.out.println(var.getVarName() + " with cost: " + var.getComputationalCost());
			}
			System.out.println();
		}
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
			/*
			 * We need to handle:
			 * 1. Node without any variable
			 * 2. Node with only control dominator
			 * 3. Node with only read variables
			 * 4. Node with only written variables
			 * 5. Node with both read and written variables
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
	 * Do nothing if no data dominatees is found
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
			
			// Do nothing if no data dominatees is found
			if (dataDominatees.isEmpty()) {
				continue;
			}
			
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
	
	/**
	 * Initialization <br><br>
	 * 
	 * Initialize the inputs and outputs
	 * probability <br><br>
	 * 
	 * Perform dynamic slicing <br><br>
	 * 
	 * Calculate alpha and beta
	 */
	private void init() {
	
//		this.slicedTrace = TraceUtil.dyanmicSlice(this.trace, this.outputs);
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
		
//		for (VarValue input : this.inputs) {
//			input.setProbability(PropProbability.HIGH);
//		}
//		for (VarValue output : this.outputs) {
//			output.setProbability(PropProbability.LOW);
//		}
		
		this.alpha = this.calAlpha(this.slicedTrace.size());
		this.beta = this.calBeta(this.slicedTrace.size());
		
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
			for (VarValue readVar : node.getReadVariables()) {
				if (!readVar.equals(wrongVar)) {
					this.inputs.add(readVar);
				}
			}
			this.addOutputs(node.getWrittenVariables());
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
}
