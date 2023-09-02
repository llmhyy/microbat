package microbat.debugpilot.propagation.BP;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class ProbInfer implements ProbabilityPropagator {

	/**
	 * The complete trace of execution of buggy program
	 */
	private Trace trace;
	
	/**
	 * Trace after dynamic slicing based on the output variable
	 */
	private List<TraceNode> slicedTrace;
	
	/**
	 * Output variables of the program, which assumed to be wrong
	 */
	private Set<VarValue> wrongVars;
	
	/**
	 * Input variables of the program, which assume to be correct
	 */
	private Set<VarValue> correctVars;
	
	private Collection<NodeFeedbacksPair> feedbackRecords = null;
	
	public ProbInfer(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getCorrectVars(), settings.getWrongVars(), settings.getFeedbacks());
	}
	
	public ProbInfer(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars, Collection<NodeFeedbacksPair> feedbackRecords) {
		Objects.requireNonNull(trace, Log.genMsg(getClass(), "Trace should not be null"));
		Objects.requireNonNull(slicedTrace, Log.genMsg(getClass(), "Slice trace should not be null"));
		Objects.requireNonNull(correctVars, Log.genMsg(getClass(), "Correct variables should not be null"));
		Objects.requireNonNull(wrongVars, Log.genMsg(getClass(), "Wrong variables should not be null"));
		Objects.requireNonNull(feedbackRecords, Log.genMsg(getClass(), "Feedbacks should not be null"));
		this.trace = trace;
		this.slicedTrace = slicedTrace;
		this.correctVars = correctVars;
		this.wrongVars = wrongVars;
		this.feedbackRecords = feedbackRecords;
	}
	
	@Override
	public void propagate() {
		Constraint.resetID();
		// Calculate the probability for variables
		VariableEncoderFG varEncoder = new VariableEncoderFG(this.trace, this.slicedTrace, this.correctVars, this.wrongVars, this.feedbackRecords);
		varEncoder.encode();
		// Calculate the probability for statements
		StatementEncoderFG statEncoder = new StatementEncoderFG(this.trace, this.slicedTrace);
		statEncoder.encode();

	}
}
