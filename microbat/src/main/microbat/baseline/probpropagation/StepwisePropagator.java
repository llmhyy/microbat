package microbat.baseline.probpropagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

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
	private List<TraceNode> slicedTrace = new ArrayList<>();
	
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
		this.init();
	}
	
	/**
	 * Initialize the inputs and outputs
	 * probability
	 */
	private void init() {
		for (VarValue input : this.inputs) {
			input.setProbability(PropProbability.HIGH);
		}
		for (VarValue output : this.outputs) {
			output.setProbability(PropProbability.LOW);
		}
	}

}
