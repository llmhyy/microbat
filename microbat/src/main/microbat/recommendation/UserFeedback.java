package microbat.recommendation;

public class UserFeedback {
	public static final String CORRECT = "correct";
	public static final String WRONG_VARIABLE_VALUE = "wrong variable value";
	public static final String WRONG_PATH = "wrong path";
	public static final String UNCLEAR = "unclear";
	
	private ChosenVariableOption option;
	private String feedbackType;
	
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
