package microbat.autofeedback;

import java.util.List;

import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;

public final class BaselineFeedbackGenerator extends FeedbackGenerator {

	/**
	 * Probability encoder
	 */
	private ProbabilityEncoder encoder;
	
	/**
	 * We assume the target to be correct if the probability of being correct is more than this threshold.
	 */
	private final double correctThreshold = 0.75;
	
	public BaselineFeedbackGenerator(Trace trace, AutoFeedbackMethods method) {
		super(trace, method);
		this.encoder = new ProbabilityEncoder(trace);
		this.encoder.setup();
		
	}
	
	/**
	 * Give feedback for the given node based on the baseline approach.
	 */
	@Override
	public UserFeedback giveFeedback(TraceNode node) {
		UserFeedback feedback = new UserFeedback();
		
		// Calculate probability of correctness
		this.encoder.encode();
		
		// Check if the node is correct or not
		if (node.getProbability() > this.correctThreshold) {
			feedback.setFeedbackType(UserFeedback.CORRECT);
			this.printFeedbackMessage(node, feedback);
			return feedback;
		}
		
		// Now the trace node is wrong
		// Get the read variable with highest probability to be wrong
		// If all the read variable is correct, then this trace node should be control incorrect.
		List<VarValue> readVars = node.getReadVariables();
		VarValue wrongVar = null;
		double minProb = this.correctThreshold;
		for (VarValue readVar: readVars) {
			if (readVar.getProbability() < minProb) {
				wrongVar = readVar;
				minProb = readVar.getProbability();
			}
		}
		
		if (wrongVar == null) {
			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
		} else {
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			ChosenVariableOption option = new ChosenVariableOption(wrongVar, null);
			feedback.setOption(option);
		}
		
		this.printFeedbackMessage(node, feedback);
		return feedback;
	}
	
	/**
	 * Directly get the root cause node
	 * @return Root cause node
	 */
	public TraceNode getErrorNode() {
		this.encoder.encode();
		return this.encoder.getMostErroneousNode();
	}
}
