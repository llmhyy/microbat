package microbat.probability.SPP.vectorization.vector;

import microbat.recommendation.UserFeedback;

public class FeedbackVariableVector extends FeedbackVector {
	
	public static final int DIMENSION = FeedbackVector.DIMENSION+1;
	
	protected static final int SIM_IDX = FeedbackVector.DIMENSION;
	
	public FeedbackVariableVector() {
		super(FeedbackVariableVector.DIMENSION);
	}
	
	public FeedbackVariableVector(final UserFeedback feedback, final float sim) {
		super(FeedbackVariableVector.DIMENSION);
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			this.set(FeedbackVector.CORRECT_IDX);
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			this.set(FeedbackVector.WRONG_PATH_IDX);
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			this.set(FeedbackVector.WRONG_VARIABLE_IDX);
		}
		this.vector[FeedbackVariableVector.SIM_IDX] = sim;
	}
}
