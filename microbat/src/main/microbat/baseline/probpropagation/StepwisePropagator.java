package microbat.baseline.probpropagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.baseline.constraints.PropagationProbability;
import microbat.model.trace.Trace;
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
	
	private Trace trace;
	
	private List<VarValue> inputs = new ArrayList<>();
	private List<VarValue> outputs = new ArrayList<>();
	
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
	
	public void propagate() {
		
	}
	
	/**
	 * Initialize the inputs and outputs
	 * probability
	 */
	public void init() {
		for (VarValue input : this.inputs) {
			input.setProbability(PropagationProbability.HIGH);
		}
	}

}
