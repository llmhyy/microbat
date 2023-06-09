package microbat.probability.SPP.propagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.BP.StatementEncoderFG;
import microbat.probability.BP.VariableEncoderFG;
import microbat.probability.BP.constraint.Constraint;
import microbat.recommendation.UserFeedback;

public class PropInfer implements ProbabilityPropagator {

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
	
	
	public PropInfer(Trace trace, List<TraceNode> slicedTrace, Set<VarValue> correctVars, Set<VarValue> wrongVars, Collection<NodeFeedbacksPair> feedbackRecords) {
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
	
	public void responseFeedbacks(NodeFeedbacksPair pair) {
		
		// Replace the feedbacks if it already exists
		this.feedbackRecords.removeIf(feedbackPair -> feedbackPair.getNode().equals(pair.getNode()));
		// Record the feedbacks
		this.feedbackRecords.add(pair);
		
		final TraceNode node = pair.getNode();
		final TraceNode controlDom = node.getControlDominator();
		
		final List<VarValue> readVars = new ArrayList<>();
		readVars.addAll(node.getReadVariables());
		readVars.removeIf(var -> var.isThisVariable());
		
		final List<VarValue> writtenVars = new ArrayList<>();
		writtenVars.addAll(node.getWrittenVariables());
		writtenVars.removeIf(var -> var.isThisVariable());
		
		if (pair.getFeedbackType().equals(UserFeedback.CORRECT)) {
			this.correctVars.addAll(readVars);
			this.correctVars.addAll(writtenVars);
			if (controlDom != null) {
				this.correctVars.add(controlDom.getConditionResult());
			}
		} else if (pair.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			if (controlDom == null) {
				throw new RuntimeException("There are no control dominator");
			}
			final VarValue controlDomVar = controlDom.getConditionResult();
			this.wrongVars.add(controlDomVar);
		} else if (pair.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			List<VarValue> wrongReadVars = new ArrayList<>();
			for (UserFeedback feedback : pair.getFeedbacks()) {
				wrongReadVars.add(feedback.getOption().getReadVar());
			}
			this.wrongVars.addAll(wrongReadVars);
			this.wrongVars.addAll(node.getWrittenVariables());
			this.correctVars.add(controlDom.getConditionResult());
		}
	}

}
