package microbat.probability.SPP.propagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import debuginfo.NodeFeedbackPair;
import debuginfo.NodeFeedbacksPair;
import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.probability.PropProbability;
import microbat.probability.SPP.ProbAggregateMethods;
import microbat.probability.SPP.ProbAggregator;
import microbat.recommendation.UserFeedback;

public class ProbPropagator {
	
	private final Trace trace;
	private final List<TraceNode> slicedTrace;
	
	private final Set<VarValue> correctVars;
	private final Set<VarValue> wrongVars;
	
	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	private Collection<NodeFeedbacksPair> feedbackRecords = null;
	
	public ProbPropagator(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars, Collection<NodeFeedbacksPair> feedbackRecords) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
		this.correctVars = correctVars;
		this.wrongVars = wrongVars;
		this.feedbackRecords = feedbackRecords;
		this.constructUnmodifiedOpcodeType();
	}
	
	public void propagate() {
		this.fuseFeedbacks();
		this.initProb();
		this.computeComputationalCost();
		this.forwardPropagate();
		this.backwardPropagate();
		this.combineProb();
	}
	
	/**
	 * Initialize the probability of each variables
	 * 
	 * Inputs are set to 0.95. <br>
	 * Outputs are set to 0.05. <br>
	 * Others are set to 0.5.
	 */
	public void initProb() {
		
		// Initialize all variables to UNCERTAIN
		this.trace.getExecutionList().stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.setAllProbability(PropProbability.UNCERTAIN));
		this.trace.getExecutionList().stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.setAllProbability(PropProbability.UNCERTAIN));
		
		// Initialize inputs to be CORRECT
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getReadVariables().stream())
									 .filter(var -> this.isCorrect(var))
									 .forEach(var -> var.setForwardProb(PropProbability.CORRECT));
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getWrittenVariables().stream())
									 .filter(var -> this.isCorrect(var))
									 .forEach(var -> var.setForwardProb(PropProbability.CORRECT));
		
		// Initialize outputs to be WRONG
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getReadVariables().stream())
									 .filter(var -> this.isWrong(var))
									 .forEach(var -> var.setBackwardProb(PropProbability.WRONG));
		this.trace.getExecutionList().stream()
									 .flatMap(node -> node.getWrittenVariables().stream())
									 .filter(var -> this.isWrong(var))
									 .forEach(var -> var.setBackwardProb(PropProbability.WRONG));
	}
	
	private void forwardPropagate() {
//		this.computeMinOutputCost();
		for (TraceNode node : this.slicedTrace) {
			
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			// Pass forward probability 
			this.passForwardProp(node);
						
			// We will ignore "this" variable
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).toList();
			
			// Skip propagation if either read or written variable is missing
			if (readVars.isEmpty() || writtenVars.isEmpty()) {
				continue;
			}
			
			// Average probability of read variables excluding "this" variable
			final double avgProb = readVars.stream().mapToDouble(var -> var.getForwardProb()).average().orElse(0.0d);
			final double drop = avgProb - PropProbability.UNCERTAIN;
			final double cost_factor = node.computationCost;
			
			final double result_prob = avgProb - drop * cost_factor;
			
			for (VarValue writtenVar : writtenVars) {
				if (this.isCorrect(writtenVar)) {
					writtenVar.setForwardProb(PropProbability.CORRECT);
				} else if (this.isWrong(writtenVar)) {
//					writtenVar.setForwardProb(PropProbability.UNCERTAIN);
				} else {
					writtenVar.setForwardProb(result_prob);
				}
			}
		}
	}
	
	private boolean isCorrect(final VarValue var) {
		return this.correctVars.contains(var);
	}
	
	private boolean isWrong(final VarValue var) {
		return this.wrongVars.contains(var);
	}
	
	private void passForwardProp(final TraceNode node) {
		// Receive the correctness propagation
		for (VarValue readVar : node.getReadVariables()) {
			if (this.isCorrect(readVar)) {
				readVar.setForwardProb(PropProbability.CORRECT);
			}
			VarValue dataDomVar = this.findDataDomVar(readVar, node);
			if (dataDomVar != null) {
				readVar.setForwardProb(dataDomVar.getForwardProb());
			} else {
				readVar.setForwardProb(PropProbability.UNCERTAIN);
			}
		}
	}
	
	private void backwardPropagate() {
		// Loop the execution list backward
		for (int order = this.slicedTrace.size()-1; order>=0; order--) {
			TraceNode node = this.slicedTrace.get(order);
			
			// Skip propagation if feedback is given
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			// Initialize written variables probability
			this.passBackwardProp(node);
			
			// We will ignore "this" variable
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
			List<VarValue> writtenVars = node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).toList();
			
			// Skip propagation if either read or written variable is missing
			if (readVars.isEmpty() || writtenVars.isEmpty()) {
				continue;
			}
			
			// Calculate the average probability of written variables excluding "this" variable
			final double avgProb = writtenVars.stream().mapToDouble(var -> var.getBackwardProb()).average().orElse(0.0d);
			final double gain = PropProbability.UNCERTAIN - avgProb;
			final double cost_factor = node.computationCost;
			node.setGain(gain * cost_factor);
			
			// Sum of computation cost of read variables excluding "this" variable
			final double sumOfCost = readVars.stream().mapToDouble(var -> var.computationalCost).sum();
			final double readVarCount = readVars.size();
			for (VarValue readVar : readVars) {
				if (this.isWrong(readVar)) {
					readVar.setBackwardProb(PropProbability.WRONG);
				} else {
					// If there are only one read variable, then it is the most suspicious
					double suspiciousness;
					if (readVars.size() == 1) {
						suspiciousness = 1.0;
					} else {
						// It is still possible that sumOfCost is zero
						// In this case, we distribute the wrongness evenly 
						suspiciousness = sumOfCost == 0.0d ?
											1 / readVarCount :
											1 - readVar.computationalCost / sumOfCost;
					}
					
					final double prob = avgProb + gain * cost_factor * suspiciousness;
					readVar.setBackwardProb(prob);
				}
			}
		}
	}
	
	private void passBackwardProp(final TraceNode node) {
		
		// Receive the wrongness propagation
		for (VarValue writtenVar : node.getWrittenVariables()) {
			
			if (this.isWrong(writtenVar)) {
				writtenVar.setBackwardProb(PropProbability.WRONG);
				continue;
			}
			
			// Different back propagation strategy for condition result
			if (node.isBranch() && writtenVar.equals(node.getConditionResult())) {
				/*
				 * Backward probability of condition result will be the average of
				 * all written variables that is under his control domination
				 * 
				 * We need to filter out those that does not contribute to the output
				 */
				final double avgWrittenProb = node.getControlDominatees().stream()
												  .filter(node_ -> this.slicedTrace.contains(node_))
												  .flatMap(node_ -> node_.getWrittenVariables().stream())
												  .mapToDouble(var -> var.getBackwardProb())
												  .average()
												  .orElse(PropProbability.UNCERTAIN);
				writtenVar.setBackwardProb(avgWrittenProb);
				continue;						  
			}
			
			List<TraceNode> dataDominatees = this.trace.findDataDependentee(node, writtenVar);
			final double maxProb = dataDominatees.stream()
					.filter(node_ -> this.slicedTrace.contains(node_))
					.flatMap(node_ -> node_.getReadVariables().stream())
					.filter(var -> var.equals(writtenVar))
					.mapToDouble(var -> var.getBackwardProb())
					.max()
					.orElse(PropProbability.UNCERTAIN);
			writtenVar.setBackwardProb(maxProb);
		}
	}

	private void combineProb() {
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				double avgProb = (readVar.getForwardProb() + readVar.getBackwardProb())/2;
				readVar.setProbability(avgProb);
			}
			for (VarValue writtenVar : node.getWrittenVariables()) {
				double avgProb = (writtenVar.getForwardProb() + writtenVar.getBackwardProb())/2;
				writtenVar.setProbability(avgProb);
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
	
	private boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			if (node.equals(pair.getNode())) {
				return true;
			}
		}
		return false;
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
	
	public void computeComputationalCost() {
		
		// First count the computational operations for each step and normalize
		final long totalNodeCost = this.slicedTrace.stream()
									   .mapToLong(node -> this.countModifyOperation(node))
									   .sum();
		
		this.slicedTrace.stream().forEach(node -> node.computationCost =  this.countModifyOperation(node) / (double) totalNodeCost);
		
		// Init computational cost of all variable to 1.0
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
									 
		double maxVarCost = 0.0f;
		for (TraceNode node : this.slicedTrace) {
			
			// Skip if there are no read variable (do not count "this" variable)
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).toList();
			if (readVars.size() == 0) {
				continue;
			}
			
			// Inherit computational cost
			for (VarValue readVar : node.getReadVariables()) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.computationalCost = dataDomVar.computationalCost;
				}
			}
			
			// Sum up the cost of all read variable, excluding "this" variable
			final double cumulatedCost = node.getReadVariables().stream().filter(var -> !var.isThisVariable())
					.mapToDouble(var -> var.computationalCost)
					.sum();
			final double optCost = node.computationCost;
			final double cost = cumulatedCost + optCost;
			
			// Assign computational cost to written variable, excluding "this" variable
			node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).forEach(var -> var.computationalCost = cost);
			maxVarCost = Math.max(cost, maxVarCost);
		}
		final double maxVarCost_ = maxVarCost;
		
		this.slicedTrace.stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost /= maxVarCost_);
		this.slicedTrace.stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost /= maxVarCost_);
		
//		for (TraceNode node : trace.getExecutionList()) {
//			System.out.println("Node: " + node.getOrder() + " cost: " + node.computationCost);
//		}
	}
	
	private void fuseFeedbacks() {
		for (NodeFeedbacksPair feedbackPair : this.feedbackRecords) {
			TraceNode node = feedbackPair.getNode();
//			UserFeedback feedback = nodeFeedbacksPair.getFeedback();
			if (feedbackPair.getFeedbackType().equals(UserFeedback.CORRECT)) {
				// If the feedback is CORRECT, then set every variable and control dom to be correct
				this.correctVars.addAll(node.getReadVariables());
				this.correctVars.addAll(node.getWrittenVariables());
				TraceNode controlDominator = node.getControlDominator();
				if (controlDominator != null) {
					VarValue controlDom = controlDominator.getConditionResult();
					this.correctVars.add(controlDom);
				}
			} else if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
				// If the feedback is WRONG_PATH, set control dominator varvalue to wrong
				TraceNode controlDominator = node.getControlDominator();
				VarValue controlDom = controlDominator.getConditionResult();
				this.wrongVars.add(controlDom);
			} else if (feedbackPair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				// If the feedback is WRONG_VARIABLE_VALUE, set selected to be wrong
				// and set control dominator to be correct
				List<VarValue> wrongReadVars = new ArrayList<>();
				for (UserFeedback feedback : feedbackPair.getFeedbacks()) {
					wrongReadVars.add(feedback.getOption().getReadVar());
				}
				for (VarValue readVar : node.getReadVariables()) {
					if (wrongReadVars.contains(readVar)) {
						this.wrongVars.add(readVar);
					} else {
//						this.addCorrectVar(readVar);
					}
				}
				this.wrongVars.addAll(node.getWrittenVariables());
				TraceNode controlDom = node.getControlDominator();
				if (controlDom != null) {
					this.correctVars.add(controlDom.getConditionResult());
				}
			}
		}
	}
	
	private void constructUnmodifiedOpcodeType() {
		this.unmodifiedType.add(OpcodeType.LOAD_CONSTANT);
		this.unmodifiedType.add(OpcodeType.LOAD_FROM_ARRAY);
		this.unmodifiedType.add(OpcodeType.LOAD_VARIABLE);
		this.unmodifiedType.add(OpcodeType.STORE_INTO_ARRAY);
		this.unmodifiedType.add(OpcodeType.STORE_VARIABLE);
		this.unmodifiedType.add(OpcodeType.RETURN);
		this.unmodifiedType.add(OpcodeType.GET_FIELD);
		this.unmodifiedType.add(OpcodeType.GET_STATIC_FIELD);
		this.unmodifiedType.add(OpcodeType.PUT_FIELD);
		this.unmodifiedType.add(OpcodeType.PUT_STATIC_FIELD);
		this.unmodifiedType.add(OpcodeType.INVOKE);
	}
}
