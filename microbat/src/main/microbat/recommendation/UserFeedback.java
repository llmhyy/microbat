package microbat.recommendation;

import microbat.model.value.VarValue;

public class UserFeedback {
	public static final String CORRECT = "correct";
	public static final String WRONG_VARIABLE_VALUE = "wrong variable value";
	public static final String WRONG_PATH = "wrong path";
	public static final String UNCLEAR = "unclear";
	public static final String ROOTCAUSE = "root cause";
//	public static final String CORRECT_VARIABLE_VALUE = "correct variable value";
	
	protected ChosenVariableOption option;
	protected String feedbackType;
	
	public UserFeedback(ChosenVariableOption option, String feedbackType) {
		super();
		this.option = option;
		this.feedbackType = feedbackType;
	}

	public UserFeedback(String feedbackType) {
		super();
		this.feedbackType = feedbackType;
	}
	
	public UserFeedback(){}
	
	public ChosenVariableOption getOption() {
		return option;
	}

	public void setOption(ChosenVariableOption option) {
		this.option = option;
	}

	public String getFeedbackType() {
		return feedbackType;
	}

	public void setFeedbackType(String feedbackType) {
		this.feedbackType = feedbackType;
	}

	/**
	 * week_equals only require the feedback type
	 * and the read variable matches
	 * @param obj Other object
	 * @return True if they are week_equals
	 */
	public boolean week_equals(Object obj) {
		if (obj instanceof UserFeedback) {
			UserFeedback otherFeedback = (UserFeedback) obj;
			
			if (!this.feedbackType.equals(otherFeedback.feedbackType)) {
				return false;
			}
			
			if (this.feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				VarValue thisVar = this.option.getReadVar();
				VarValue otherVar = otherFeedback.option.getReadVar();
				return thisVar.equals(otherVar);
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		if(option != null){
			return "UserFeedback [option=" + option + ", feedbackType=" + feedbackType + "]";			
		}
		else{
			return "UserFeedback [feedbackType=" + feedbackType + "]";
		}
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof UserFeedback){
			UserFeedback otherFeedback = (UserFeedback)obj;
			
			String thisString = this.toString();
			String thatString = otherFeedback.toString();
			
			return thisString.equals(thatString);
		}
		
		return false;
	}
}
