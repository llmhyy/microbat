package microbat.probability.SPP.propagation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import debuginfo.NodeFeedbackPair;
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
	
	private final List<VarValue> correctVars;
	private final List<VarValue> wrongVars;
	
	private final ProbAggregator aggregator = new ProbAggregator();
	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	
	private List<NodeFeedbackPair> feedbackRecords = new ArrayList<>();
	
	public ProbPropagator(Trace trace, List<TraceNode> slicedTrace, List<VarValue> correctVars, List<VarValue> wrongVars, List<NodeFeedbackPair> feedbackRecords) {
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
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setAllProbability(PropProbability.UNCERTAIN);
				if (this.correctVars.contains(readVar)) {
					readVar.setAllProbability(PropProbability.HIGH);
				}
				if (this.wrongVars.contains(readVar)) {
					readVar.setAllProbability(PropProbability.LOW);
				}
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				writeVar.setAllProbability(PropProbability.UNCERTAIN);
				if (this.correctVars.contains(writeVar)) {
					writeVar.setAllProbability(PropProbability.HIGH);
				}
				if (this.wrongVars.contains(writeVar)) {
					writeVar.setAllProbability(PropProbability.LOW);
				}
			}
		}
	}
	
	private void forwardPropagate() {
		this.computeMinOutputCost();
		for (TraceNode node : this.slicedTrace) {
			
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
			
			double avgProb = this.aggregator.aggregateForwardProb(readVars, ProbAggregateMethods.AVG);
			if (avgProb <= PropProbability.LOW) {
				// No need to continue if the avgProb is already LOW
				for (VarValue writtenVar : node.getWrittenVariables()) {
					if (this.correctVars.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.HIGH);
					} else if (this.wrongVars.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.LOW);
					} else {
						writtenVar.setForwardProb(avgProb);
					}
				}
				continue;
			}
			
			if (node.isBranch()) {
				for (VarValue writtenVar : node.getWrittenVariables()) {
					if (this.wrongVars.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.LOW);
					} else {
						writtenVar.setForwardProb(avgProb);
					}
				}
			} else {
				// Calculate forward probability of written variable
				long writtenCost = node.getWrittenVariables().get(0).getComputationalCost();
				
				// Find the closest wrong variable computational cost 
				long outputCost = node.getMinOutpuCost();
				
				double loss = (avgProb - PropProbability.LOW) * ((double) writtenCost / outputCost);
				double prob = avgProb - loss;
				if (prob < 0) {
					System.out.println();
				}
				for (VarValue writtenVar : node.getWrittenVariables()) {
					if (this.correctVars.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.HIGH);
					} else if (this.wrongVars.contains(writtenVar)) {
						writtenVar.setAllProbability(PropProbability.LOW);
					} else {
						writtenVar.setForwardProb(prob);
					}
				}
			}
		}
	}
	
	private void passForwardProp(final TraceNode node) {
		// Receive the correctness propagation
		for (VarValue readVar : node.getReadVariables()) {
			
			// Ignore the input variables such that it will not be overwritten
			if (this.correctVars.contains(readVar)) {
				readVar.setAllProbability(PropProbability.HIGH);
				continue;
			}
			
			if (this.wrongVars.contains(readVar)) {
				readVar.setAllProbability(PropProbability.LOW);
				continue;
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
				System.out.println("TraceNode: " + node.getOrder() + " is skipped because feedback is already given");
				continue;
			}
			
			// Initialize written variables probability
			this.passBackwardProp(node);
			
			// Skip when there are no either read or written variables
			if (node.getReadVariables().isEmpty() || node.getWrittenVariables().isEmpty()) {
				System.out.println("TraceNode: " + node.getOrder() + " is skipped because there are no either read or written variable");
				continue;
			}
			
			// Aggregate written variable probability
			double avgProb = this.aggregator.aggregateBackwardProb(node.getWrittenVariables(), ProbAggregateMethods.AVG);
			
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
				if (this.wrongVars.contains(readVar) || this.correctVars.contains(readVar)) {
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
	
	private void passBackwardProp(final TraceNode node) {
		
		// Receive the wrongness propagation
		for (VarValue writeVar : node.getWrittenVariables()) {
			
			// Ignore the output variable such that it will not be overwritten
			if (this.wrongVars.contains(writeVar)) {
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
			
			if (this.correctVars.contains(conditionResult)) {
				conditionResult.setAllProbability(PropProbability.HIGH);
			} else if (this.wrongVars.contains(conditionResult)) {
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

	private void combineProb() {
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setProbability(readVar.getBackwardProb());
			}
			for (VarValue writtenVar : node.getWrittenVariables()) {
				writtenVar.setProbability(writtenVar.getBackwardProb());
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
		for (NodeFeedbackPair pair : this.feedbackRecords) {
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
	
	private void computeMinOutputCost() {
		for (TraceNode node : this.slicedTrace) {
			node.setMinOutputCost(Long.MAX_VALUE);
		}
		TraceNode lastNode = this.slicedTrace.get(this.slicedTrace.size()-1);
		if (!lastNode.getWrittenVariables().isEmpty()) {
			lastNode.setMinOutputCost(lastNode.getWrittenVariables().get(0).getComputationalCost());
		} else {
			long maxComputationalCost = 0;
			for (VarValue readVar : lastNode.getReadVariables()) {
				if (readVar.getComputationalCost() > maxComputationalCost) {
					maxComputationalCost = readVar.getComputationalCost();
				}
			}
			lastNode.setMinOutputCost(maxComputationalCost);
		}
		
		Queue<TraceNode> toVisitNodes = new LinkedList<>();
		toVisitNodes.add(lastNode);
		
		Set<TraceNode> visitedNodes = new HashSet<>();

		while(!toVisitNodes.isEmpty()) {
			TraceNode node = toVisitNodes.poll();
			visitedNodes.add(node);
			
			for (VarValue readVar : node.getReadVariables()) {
				if (this.wrongVars.contains(readVar)) {
					node.setMinOutputCost(readVar.getComputationalCost());
				}
			}
			for (VarValue writtenVar : node.getWrittenVariables()) {
				if (this.wrongVars.contains(writtenVar)) {
					node.setMinOutputCost(writtenVar.getComputationalCost());
				}
			}
			
			final long minOutput = node.getMinOutpuCost();
			
			for (VarValue readVar : node.getReadVariables()) {
				TraceNode dataDom = this.trace.findDataDependency(node, readVar);
				if (dataDom == null) {
					continue;
				}
				
				if (visitedNodes.contains(dataDom) && dataDom.getMinOutpuCost() < minOutput) {
					continue;
				}
				
				if (dataDom.getMinOutpuCost() > minOutput) {
					dataDom.setMinOutputCost(minOutput);
				}
				
				if (!toVisitNodes.contains(dataDom)) {
					toVisitNodes.add(dataDom);
				}
			}
		}
	}
	
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
	
	private void constructUnmodifiedOpcodeType() {
		this.unmodifiedType.add(OpcodeType.LOAD_CONSTANT);
		this.unmodifiedType.add(OpcodeType.LOAD_FROM_ARRAY);
		this.unmodifiedType.add(OpcodeType.LOAD_VARIABLE);
		this.unmodifiedType.add(OpcodeType.STORE_INTO_ARRAY);
		this.unmodifiedType.add(OpcodeType.STORE_VARIABLE);
		this.unmodifiedType.add(OpcodeType.RETURN);
	}
}
