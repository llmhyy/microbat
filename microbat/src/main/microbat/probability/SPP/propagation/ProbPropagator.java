package microbat.probability.SPP.propagation;

import java.util.ArrayList;
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
import microbat.probability.PropProbability;
import microbat.probability.SPP.ProbAggregateMethods;
import microbat.probability.SPP.ProbAggregator;

public class ProbPropagator {
	
	private final Trace trace;
	private final List<TraceNode> slicedTrace;
	
	private final Set<VarValue> correctVars;
	private final Set<VarValue> wrongVars;
	
	private final ProbAggregator aggregator = new ProbAggregator();
	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	
	private List<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	
	private double max_var_cost = -1.0d;
	private int total_node_cost = -1;
	
	public ProbPropagator(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars, List<NodeFeedbacksPair> feedbackRecords) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
		this.correctVars = correctVars;
		this.wrongVars = wrongVars;
		this.feedbackRecords = feedbackRecords;
		this.constructUnmodifiedOpcodeType();
	}
	
	public void propagate() {
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
		for (TraceNode node : this.trace.getExecutionList()) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setAllProbability(PropProbability.UNCERTAIN);
				if (this.correctVars.contains(readVar)) {
					readVar.setForwardProb(PropProbability.HIGH);
				}
				if (this.wrongVars.contains(readVar)) {
					readVar.setBackwardProb(PropProbability.LOW);
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				writeVar.setAllProbability(PropProbability.UNCERTAIN);
				if (this.correctVars.contains(writeVar)) {
					writeVar.setForwardProb(PropProbability.HIGH);
				}
				if (this.wrongVars.contains(writeVar)) {
					writeVar.setBackwardProb(PropProbability.LOW);
				}
			}
		}
	}
	
	private void forwardPropagate() {
//		this.computeMinOutputCost();
		for (TraceNode node : this.slicedTrace) {
			
			
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			// Skip propagation if either read or written variable is missing
			if (node.getReadVariables().isEmpty() || node.getWrittenVariables().isEmpty()) {
				continue;
			}
			
			// Pass forward probability 
			this.passForwardProp(node);
			
			// Deep copy the array list
			List<VarValue> readVars = new ArrayList<>();
			readVars.addAll(node.getReadVariables());
			
			// Ignore this variable
			readVars.removeIf(element -> (element.isThisVariable()));
			if (readVars.isEmpty()) {
				continue;
			}
			
			final double avgProb = this.aggregator.aggregateForwardProb(readVars, ProbAggregateMethods.AVG);
			final double drop = avgProb - PropProbability.UNCERTAIN;
			final double cost_factor = this.countModifyOperation(node) / (double) this.total_node_cost;
			
			final double result_prob = avgProb - drop * cost_factor;
			
			for (VarValue writtenVar : node.getWrittenVariables()) {
				if (writtenVar.isThisVariable()) {
					continue;
				}
				if (this.isCorrect(writtenVar)) {
					writtenVar.setForwardProb(PropProbability.HIGH);
				} else {
					writtenVar.setForwardProb(result_prob);
				}
			}
//			
//			if (avgProb <= PropProbability.UNCERTAIN) {
//				// No need to continue if the avgProb is already LOW
//				for (VarValue writtenVar : node.getWrittenVariables()) {
//					if (this.correctVars.contains(writtenVar)) {
//						writtenVar.setAllProbability(PropProbability.HIGH);
//					} else if (this.wrongVars.contains(writtenVar)) {
//						writtenVar.setAllProbability(PropProbability.LOW);
//					} else {
//						writtenVar.setForwardProb(PropProbability.UNCERTAIN);
//					}
//				}
//				continue;
//			}
//			
//			if (node.isBranch()) {
//				for (VarValue writtenVar : node.getWrittenVariables()) {
//					if (this.wrongVars.contains(writtenVar)) {
//						writtenVar.setAllProbability(PropProbability.LOW);
//					} else {
//						writtenVar.setForwardProb(avgProb);
//					}
//				}
//			} else {
//				// Calculate forward probability of written variable
//				double writtenCost = node.getWrittenVariables().get(0).getComputationalCost();
//				double optCost = Double.MIN_VALUE * this.countModifyOperation(node) / this.max_var_cost;
//				
//				// Find the closest wrong variable computational cost 
////				long outputCost = node.getMinOutpuCost();
////				double loss = avgProb - PropProbability.LOW;
////				double loss = (avgProb - PropProbability.LOW) * ((double) writtenCost / outputCost);
////				double prob = avgProb - loss;
//				
//				double discount = 1 - ((double) optCost / writtenCost);
//
//				if (discount == 0 || writtenCost == 0.0) discount = 1;
//				double prob = avgProb * discount;
//				prob = Math.max(prob, PropProbability.UNCERTAIN);
//				if (prob < 0) {
//					throw new RuntimeException("Problematic probability at node: " + node.getOrder() + ": " + prob);
//				}
// 				for (VarValue writtenVar : node.getWrittenVariables()) {
//					if (this.correctVars.contains(writtenVar)) {
//						writtenVar.setAllProbability(PropProbability.HIGH);
//					} else if (this.wrongVars.contains(writtenVar)) {
//						writtenVar.setAllProbability(PropProbability.LOW);
//					} else {
//						writtenVar.setForwardProb(prob);
//					}
//				}
//			}
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
				readVar.setForwardProb(PropProbability.HIGH);
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
			
			// Skip this node if the feedback is already given
			if (this.isFeedbackGiven(node)) {
				continue;
			}
			
			// Initialize written variables probability
			this.passBackwardProp(node);
			
			// Skip when there are no either read or written variables
			if (node.getReadVariables().isEmpty() || node.getWrittenVariables().isEmpty()) {
				continue;
			}
			
			final double avgProb = this.aggregator.aggregateBackwardProb(node.getWrittenVariables(), ProbAggregateMethods.AVG);
			final double gain = PropProbability.UNCERTAIN - avgProb;
			final double cost_factor = this.countModifyOperation(node) / this.total_node_cost;
			node.setGain(gain * cost_factor);
			List<VarValue> readVars = node.getReadVariables();
			readVars.removeIf(var -> var.isThisVariable());
			
			double sum = 0.0d;
			for (VarValue readVar : readVars) {
				sum += readVar.getComputationalCost();
			}
			
			sum = sum == 0 ? 1 : sum;
			
			for (VarValue readVar : readVars) {
				if (this.isWrong(readVar)) {
					readVar.setBackwardProb(PropProbability.LOW);
				} else {
					final double suspiciousness = readVar.getComputationalCost() / sum;
					final double prob = avgProb + gain * cost_factor * suspiciousness;
					readVar.setBackwardProb(prob);
				}
				
			}
		}
			
//			// Calculate maximum gain
//			VarValue writtenVar = node.getWrittenVariables().get(0);
//			double cumulativeCost = writtenVar.getComputationalCost();
//			double opCost = Double.MIN_VALUE * this.countModifyOperation(node) / this.max_var_cost;
////			double gain = 0;
//			if (cumulativeCost != 0) {
//				// Define maximum gain by the step
//				gain = (PropProbability.UNCERTAIN - avgProb) * ((double) opCost/cumulativeCost);
//			}
//			
//			node.setGain(gain);
//			
//			// Calculate total cost
//			int totalCost = 0;
//			for (VarValue readVar : node.getReadVariables()) {
//				totalCost += readVar.getComputationalCost();
//			}
//	
//			for (VarValue readVar : node.getReadVariables()) {
//				
//				// Ignore this variable if it is input or output
//				if (this.wrongVars.contains(readVar) || this.correctVars.contains(readVar)) {
//					continue;
//				}
//				
//				if (readVar.isThisVariable()) {
//					readVar.setBackwardProb(PropProbability.HIGH);
//					continue;
//				}
//				
//				double factor = 1;
//				if (totalCost != 0) {
//					if (readVar.getComputationalCost() != totalCost) {
//						factor = 1 - readVar.getComputationalCost() / (double) totalCost;
//					}
//				}
//				
//				double prob = avgProb + gain  * factor;
//				readVar.setBackwardProb(prob);
//				
//				
//			}
//		}
//		
//		double max_gain = -2.0d;
//		int max_order = -1;
//		for (TraceNode node : this.slicedTrace) {
//			final double gain = node.getGain();
//			if (gain > max_gain) {
//				max_gain = gain;
//				max_order = node.getOrder();
//			}
//		}
//		
//		System.out.println("max_gain" + max_gain);
//		System.out.println("max_order" + max_order);
//		
//		int max_count = -1;
//		max_order = -1;
//		for (TraceNode node : this.slicedTrace) {
//			final int count = this.countModifyOperation(node);
//			if (max_count < count) {
//				max_count = count;
//				max_order = node.getOrder();
//			}
//		}
//		
//		System.out.println("max_count" + max_count);
//		System.out.println("max_order" + max_order);
	}
	
	private void passBackwardProp(final TraceNode node) {
		
		// Receive the wrongness propagation
		for (VarValue writeVar : node.getWrittenVariables()) {
			
			if (this.isWrong(writeVar)) {
				writeVar.setBackwardProb(PropProbability.LOW);
				continue;
			}
			
			List<TraceNode> dataDominatees = this.trace.findDataDependentee(node, writeVar);
			// Filter out the dominatees that does not contribute to the result
			dataDominatees.removeIf(dataDominatee -> !this.slicedTrace.contains(dataDominatee));
			
			// Do nothing if no data dominatees is found
			if (dataDominatees.isEmpty()) {
				writeVar.setBackwardProb(PropProbability.UNCERTAIN);
			} else {
				// Pass the largest probability
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
		
		// Backward probability of condition result is calculated as
		// average of written variables probability in it's control scope
		if (node.isBranch()) {
			VarValue conditionResult = node.getConditionResult();
			
			if (this.isWrong(conditionResult)) {
				conditionResult.setBackwardProb(PropProbability.LOW);
			} else {
				List<TraceNode> controlDominatees = node.getControlDominatees();
				controlDominatees.removeIf(controlDominatee -> !this.slicedTrace.contains(controlDominatee));
				
				double avgProb = 0.0;
				int count = 0;
				for (TraceNode controlDominatee : node.getControlDominatees()) {
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
	
//	private void computeMinOutputCost() {
//		for (TraceNode node : this.slicedTrace) {
//			node.setMinOutputCost(Long.MAX_VALUE);
//		}
//		TraceNode lastNode = this.slicedTrace.get(this.slicedTrace.size()-1);
//		if (!lastNode.getWrittenVariables().isEmpty()) {
//			lastNode.setMinOutputCost(lastNode.getWrittenVariables().get(0).getComputationalCost());
//		} else {
//			long maxComputationalCost = 0;
//			for (VarValue readVar : lastNode.getReadVariables()) {
//				if (readVar.getComputationalCost() > maxComputationalCost) {
//					maxComputationalCost = readVar.getComputationalCost();
//				}
//			}
//			lastNode.setMinOutputCost(maxComputationalCost);
//		}
//		
//		Queue<TraceNode> toVisitNodes = new LinkedList<>();
//		toVisitNodes.add(lastNode);
//		
//		Set<TraceNode> visitedNodes = new HashSet<>();
//
//		while(!toVisitNodes.isEmpty()) {
//			TraceNode node = toVisitNodes.poll();
//			visitedNodes.add(node);
//			
//			for (VarValue readVar : node.getReadVariables()) {
//				if (this.wrongVars.contains(readVar)) {
//					node.setMinOutputCost(readVar.getComputationalCost());
//				}
//			}
//			for (VarValue writtenVar : node.getWrittenVariables()) {
//				if (this.wrongVars.contains(writtenVar)) {
//					node.setMinOutputCost(writtenVar.getComputationalCost());
//				}
//			}
//			
//			final long minOutput = node.getMinOutpuCost();
//			
//			for (VarValue readVar : node.getReadVariables()) {
//				TraceNode dataDom = this.trace.findDataDependency(node, readVar);
//				if (dataDom == null) {
//					continue;
//				}
//				
//				if (visitedNodes.contains(dataDom) && dataDom.getMinOutpuCost() < minOutput) {
//					continue;
//				}
//				
//				if (dataDom.getMinOutpuCost() > minOutput) {
//					dataDom.setMinOutputCost(minOutput);
//				}
//				
//				if (!toVisitNodes.contains(dataDom)) {
//					toVisitNodes.add(dataDom);
//				}
//			}
//		}
//	}
	
	public void computeComputationalCost() {
		
		// First count the computational operations for each step
		this.total_node_cost = 0;
		for (TraceNode node : this.slicedTrace) {
			final int count = this.countModifyOperation(node);
			node.setComputationCost(count);
			this.total_node_cost += count;
		}
		
		
		double max_cost = 0.0f;
		for (TraceNode node : this.slicedTrace) {
	
			// Inherit the computation cost from data dominator
			for (VarValue readVar : node.getReadVariables()) {
				final VarValue dataDomVar = this.findDataDomVar(readVar, node);
				if (dataDomVar != null) {
					readVar.setComputationalCost(dataDomVar.getComputationalCost());
				}
			}
			
			// Sum of read variables computational cost
			double cumulatedCost = 0.0d;
			for (VarValue readVar : node.getReadVariables()) {
				if (readVar.isThisVariable()) {
					continue;
				}
				cumulatedCost += readVar.getComputationalCost();
			}
			
			// Operational cost
			double opCost = Double.MIN_VALUE * this.countModifyOperation(node);
			double cost = cumulatedCost + opCost;
			
//			if (cost > max_cost) {
//				max_cost = cost;
//				order = node.getOrder();
//			}
			max_cost = Math.max(cost, max_cost);
			
			// Define written variables computational cost
			for (VarValue writtenVar : node.getWrittenVariables()) {
				if (writtenVar.isThisVariable()) {
					continue;
				}
				writtenVar.setComputationalCost(cost);
			}
		}
		
		this.max_var_cost = max_cost;
		
		// Normalization
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				final double cost = readVar.getComputationalCost();
				readVar.setComputationalCost(cost / max_cost);
			}
			for (VarValue writtenVar : node.getWrittenVariables()) {
				final double cost = writtenVar.getComputationalCost();
				writtenVar.setComputationalCost(cost / max_cost);
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
	}
}
