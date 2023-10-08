package microbat.debugpilot.propagation.BP;

import microbat.debugpilot.NodeFeedbackPair;
import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;
import microbat.util.UniquePriorityQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Belief Propagation follow the procedure in ICSE18 paper
 * It will first calculate probability of correctness of variable using sum-product algorithm,
 * and then calculate of correctness of each statement by brute force
 * @author David, Siang Hwee 
 *
 */
public class BeliefPropagation {
	
	
	/**
	 * The complete trace of execution of buggy program
	 */
	private Trace trace;
	
	/**
	 * Trace after dynamic slicing based on the output variable
	 */
	private List<TraceNode> slicedTrace = new ArrayList<>();
	
	/**
	 * Output variables of the program, which assumed to be wrong
	 */
	private List<VarValue> wrongVars = new ArrayList<>();
	
	/**
	 * Input variables of the program, which assume to be correct
	 */
	private List<VarValue> correctVars = new ArrayList<>();
	
	private List<NodeFeedbacksPair> feedbackRecords = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param trace Trace of testing program
	 */
	public BeliefPropagation(Trace trace, Collection<VarValue> inputs, Collection<VarValue> outputs) {
		this.trace = trace;
		this.slicedTrace = TraceUtil.dyanmicSlice(trace, outputs);
		this.correctVars.addAll(inputs);
		this.wrongVars.addAll(outputs);
	}
	
	/**
	 * Calculate the probability of the variable and statements
	 */
	public void encode() {

//		Constraint.resetID();
//		
//		// Calculate the probability for variables
//		VariableEncoderFG varEncoder = new VariableEncoderFG(this.trace, this.slicedTrace, this.correctVars, this.wrongVars, this.feedbackRecords);
//		varEncoder.encode();	
//		
//		// Calculate the probability for statements
//		StatementEncoderFG statEncoder = new StatementEncoderFG(this.trace, this.slicedTrace);
//		statEncoder.encode();

	}
	

	
	/**
	 * Get the trace node with the highest probability to be wrong
	 * 
	 * It will ignore the last node in list, which is assumed to be
	 * error node.
	 * 
	 * @return Predicted wrong node
	 */
	public TraceNode getMostErroneousNode() {
		TraceNode errorNode = null;
		double minProb = 2.0;
		for (TraceNode node : this.slicedTrace) {
			if (!this.isFeedbackGiven(node) && node.getCorrectness() < minProb && node.getCorrectness() != -1.0) {
				minProb = node.getCorrectness();
				errorNode = node;
			}
		}
		return errorNode;
	}
	
	public void responseFeedbacks(NodeFeedbacksPair pair) {
		
		// Replace the feedbacks if it already exists
		this.feedbackRecords.removeIf(feedbackPair -> feedbackPair.getNode().equals(pair.getNode()));
		// Record the feedbacks
		this.feedbackRecords.add(pair);
		
		final TraceNode node = pair.getNode();
		final TraceNode controlDom = node.getControlDominator();
		
//		final List<VarValue> readVars = new ArrayList<>();
//		readVars.addAll(node.getReadVariables());
//		readVars.removeIf(var -> var.isThisVariable());
//		
//		final List<VarValue> writtenVars = new ArrayList<>();
//		writtenVars.addAll(node.getWrittenVariables());
//		writtenVars.removeIf(var -> var.isThisVariable());
		
		final List<VarValue> readVars = node.getReadVariables();
		final List<VarValue> writtenVars = node.getWrittenVariables();
		
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
	
	private boolean isFeedbackGiven(final TraceNode node) {
		for (NodeFeedbacksPair pair : this.feedbackRecords) {
			final TraceNode node_ = pair.getNode();
			if (node_.equals(node)) {
				return true;
			}
		}
		return false;
	}	
	
}
