package microbat.probability.SPP.vectorization.vector;

import microbat.recommendation.UserFeedback;

public class FeedbackVector extends Vector {
	
	public static final int FEEDBACK_COUNT = 3;
	public static final int DIMENSION = FeedbackVector.FEEDBACK_COUNT;
	
	private static final int CORRECT_IDX = 0;
	private static final int WRONG_PATH_IDX = 1;
	private static final int WRONG_VARIABLE_IDX = 2;
	
	public FeedbackVector() {
		super(FeedbackVector.DIMENSION);
	}
	
	public FeedbackVector(final float[] vector) {
		super(vector);
	}
	
	public FeedbackVector(final UserFeedback feedback) {
		super(FeedbackVector.DIMENSION);
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			this.set(FeedbackVector.CORRECT_IDX);
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			this.set(FeedbackVector.WRONG_PATH_IDX);
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			this.set(FeedbackVector.WRONG_VARIABLE_IDX);
		}
	}
}
