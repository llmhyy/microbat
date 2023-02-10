package microbat.recommendation;

import java.util.List;

import microbat.model.value.VarValue;

public class UserFeedback_M extends UserFeedback {
	
	public UserFeedback_M(ChosenVariablesOption option, String feedbackType) {
		super(option, feedbackType);
	}
	
	public UserFeedback_M(String feedbackType) {
		super(feedbackType);
	}
	
	public UserFeedback_M() {
		super();
	}
	
	public List<VarValue> getSelectedReadVars() {
		ChosenVariablesOption option = (ChosenVariablesOption) this.option;
		return option.getReadVars();
	}
	
	public List<VarValue> getSelectedWrittenVars() {
		ChosenVariablesOption option = (ChosenVariablesOption) this.option;
		return option.getWrittenVars();
	}
	
	@Override
	public boolean week_equals(Object obj) {
		if (obj instanceof UserFeedback_M) {
			UserFeedback_M otherFeedback = (UserFeedback_M) obj;
			if (!this.feedbackType.equals(otherFeedback.feedbackType)) {
				return false;
			}
			if (this.feedbackType.equals(UserFeedback_M.WRONG_VARIABLE_VALUE)) {
				ChosenVariablesOption thisOption = (ChosenVariablesOption) this.option;
				List<VarValue> thisVars = thisOption.getReadVars();	
				ChosenVariablesOption otherOption = (ChosenVariablesOption) otherFeedback.option;
				List<VarValue> otherVars = otherOption.getReadVars();
				for (VarValue var : thisVars) {
					if (otherVars.contains(var)) {
						return true;
					}
				}
				return false;
			} else {
				return true;
			}
		}
		if (obj instanceof UserFeedback) {
			UserFeedback otherFeedback = (UserFeedback) obj;
			if (!this.feedbackType.equals(otherFeedback.feedbackType)) {
				return false;
			}
			if (this.feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				ChosenVariablesOption thisOption = (ChosenVariablesOption) this.option;
				List<VarValue> thisVars = thisOption.getReadVars();
				return thisVars.contains(otherFeedback.option.getReadVar());
			} else {
				return true;
			}
		}
		return false;
	}
}
