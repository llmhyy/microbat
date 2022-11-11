package microbat.baseline.probpropagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
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
	 * List of ouptut variables which assumed to be wrong
	 */
	private List<VarValue> outputs = new ArrayList<>();
	
	/**
	 * List of executed trace node after dynamic slicing
	 */
	private List<TraceNode> slicedTrace = null;
	
	/**
	 * Correctness Propagation Factor
	 */
	private double alpha = 1.0;
	
	/**
	 * Wrongness Propagation Factor
	 */
	private double beta = 1.0;
	
	/**
	 * Constructor
	 * @param trace Execution trace for target program
	 */
	public StepwisePropagator(Trace trace) {
		this.trace = trace;
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
	 * Propagation the correctness probability
	 * step by step
	 */
	public void propagate() {
		
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
					writeVar.setProbability(writeProb < 0.5 ? 0.5 : writeProb);
				}
			}

			// Wrongness propagation
			TraceNode backwardNode = this.slicedTrace.get(backward_ptr);
			double readProb = this.backwardProp(backwardNode);
			if (readProb != 2.0) {
				for (VarValue readVar : backwardNode.getReadVariables()) {
					readVar.setProbability(readProb > 0.5 ? 0.5 : readProb);
				}
			}
			
			forward_ptr += 1;
			backward_ptr -= 1;
		}
		
		TraceNode middleNode = this.slicedTrace.get(forward_ptr);
		if (forward_ptr == backward_ptr) {
			this.receiveForwardProp(middleNode);
			this.receiveBackwardProp(middleNode);
		} else if (forward_ptr > backward_ptr) {
			this.receiveForwardProp(middleNode);
		}
		
		
	}
	
	/**
	 * Propagate the correctness probability
	 * from read variables to written variables
	 * @param node Current trace node
	 * @return Propagated probability
	 */
	private double forwardProp(final TraceNode node) {
		this.receiveForwardProp(node);
		
		double maxProb = -1.0;
		for (VarValue readVar : node.getReadVariables()) {
			maxProb = Math.max(maxProb, readVar.getProbability());
		}
		
		if (maxProb == -1.0) {
			return -1.0;
		} else {
			return this.alpha * maxProb;
		}
	}
	
	/**
	 * Propagate the wrongness probability
	 * from written variables to written variables
	 * @param node Current trace node
	 * @return Propagated probability
	 */
	private double backwardProp(final TraceNode node) {
		this.receiveBackwardProp(node);
		
		double minProb = 2.0;
		for (VarValue writeVar : node.getWrittenVariables()) {
			minProb = Math.min(minProb, writeVar.getProbability());
		}
		
		if (minProb == 2.0) {
			return 2.0;
		} else {
			return this.beta * minProb;
		}
	}
	
	private void receiveForwardProp(final TraceNode node) {
		// Receive the correctness propagation
		for (VarValue readVar : node.getReadVariables()) {
			
			final String varID = Variable.truncateSimpleID(readVar.getVarID());
			final String headID = Variable.truncateSimpleID(readVar.getAliasVarID());
			
			TraceNode dataDominator = this.trace.findDataDependency(node, readVar);
			if (dataDominator != null) {
				for (VarValue writeVar : dataDominator.getWrittenVariables()) {
					
					final String wVarID = Variable.truncateSimpleID(writeVar.getVarID());
					final String wHeadID = Variable.truncateSimpleID(writeVar.getAliasVarID());
					
					if(wVarID != null && wVarID.equals(varID)) {
						readVar.setProbability(writeVar.getProbability());					
					} else if (wHeadID != null && wHeadID.equals(headID)) {
						readVar.setProbability(writeVar.getProbability());	
					} else {
						VarValue childValue = writeVar.findVarValue(varID, headID);
						if(childValue != null) {
							readVar.setProbability(writeVar.getProbability());	
						}
					}
					
					
				}
			}
		}
	}
	
	private void receiveBackwardProp(final TraceNode node) {
		// Receive the wrongness propagation
		for (VarValue writeVar : node.getWrittenVariables()) {
			
			final String varID = Variable.truncateSimpleID(writeVar.getVarID());
			final String headID = Variable.truncateSimpleID(writeVar.getAliasVarID());
			
			List<TraceNode> dataDominatees = this.trace.findDataDependentee(node, writeVar);
			double minProb = 2.0;
			for (TraceNode dataDominate : dataDominatees) {
				for (VarValue readVar : dataDominate.getReadVariables()) {
					
					final String rVarID = Variable.truncateSimpleID(readVar.getVarID());
					final String rHeadID = Variable.truncateSimpleID(readVar.getAliasVarID());
					
					if(rVarID != null && rVarID.equals(varID)) {
						final double prob = readVar.getProbability();
						minProb = Math.min(prob, minProb);
					} else if (rHeadID != null && rHeadID.equals(headID)) {
						final double prob = readVar.getProbability();
						minProb = Math.min(prob, minProb);
					} else {
						VarValue childValue = readVar.findVarValue(varID, headID);
						if(childValue != null) {
							final double prob = readVar.getProbability();
							minProb = Math.min(prob, minProb);
						}
					}
				}
			}
			if (minProb != 2.0) {
				writeVar.setProbability(minProb);
			}
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
	
		this.slicedTrace = TraceUtil.dyanmicSlice(this.trace, this.inputs);
		for (TraceNode node : this.slicedTrace) {
			for (VarValue readVar : node.getReadVariables()) {
				readVar.setProbability(PropProbability.UNCERTAIN);
			}
			for (VarValue writeVar : node.getWrittenVariables()) {
				writeVar.setProbability(PropProbability.UNCERTAIN);
			}
		}
		
		for (VarValue input : this.inputs) {
			input.setProbability(PropProbability.HIGH);
		}
		for (VarValue output : this.outputs) {
			output.setProbability(PropProbability.LOW);
		}
		
		this.alpha = this.calAlpha(this.slicedTrace.size());
		this.beta = this.calBeta(this.slicedTrace.size());
	}
	
	private double calAlpha(final int traceLen) {
		double alpha = PropProbability.LOW / PropProbability.HIGH;
		alpha = Math.pow(alpha, (double) 1/(2*traceLen));
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

}
