package microbat.views.utils.manager;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Button;

import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.debugpilot.userfeedback.DPUserFeedbackType;
import microbat.log.Log;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

public class FeedbackSelectionManager {
	
	protected Map<UserFeedback, YesNoButtonPair> feedbackYesNoButtonsMap = new HashMap<>();
	
	public FeedbackSelectionManager() {
		
	}
	
	public void registerYesCheckbox(final UserFeedback feedback, final Button checkbox) {
		if (this.feedbackYesNoButtonsMap.containsKey(feedback)) {
			this.feedbackYesNoButtonsMap.get(feedback).setYesButton(checkbox);
		} else {
			this.feedbackYesNoButtonsMap.put(feedback, new YesNoButtonPair(checkbox, null));
		}
	}
	
	public void registerNoCheckbox(final UserFeedback feedback, final Button checkbox) {
		if (this.feedbackYesNoButtonsMap.containsKey(feedback)) {
			this.feedbackYesNoButtonsMap.get(feedback).setNoButton(checkbox);
		} else {
			this.feedbackYesNoButtonsMap.put(feedback, new YesNoButtonPair(null, checkbox));
		}
	}
	
	public void verify(UserFeedback[] allAvaiableFeedbacks) {
		if (this.feedbackYesNoButtonsMap.size() != allAvaiableFeedbacks.length) {
			throw new RuntimeException(Log.genMsg(getClass(), "YesNoButtonPairs count mismatch with number of feedback"));
		}
		
		for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
		    final UserFeedback feedback = entry.getKey();
		    final YesNoButtonPair pair = entry.getValue();
		    if (!pair.isValid()) {
		    	throw new RuntimeException(Log.genMsg(getClass(), "Button pair is not valid for feedback: " + feedback));
		    }
		}
	}
	
	public void dispose() {
		for (YesNoButtonPair pair : this.feedbackYesNoButtonsMap.values()) {
			pair.dispose();
		}
		this.feedbackYesNoButtonsMap.clear();
	}
	
	public DPUserFeedback genDpUserFeedback(final TraceNode node) {
		// This step is root cause
		YesNoButtonPair rootCauseSelection = this.feedbackYesNoButtonsMap.get(this.genRootCuaseFeedbackKey());
		if (rootCauseSelection.isYesButtonSelected()) {
			return new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, node);
		}
		
		// This step is correct
		YesNoButtonPair correctSelection = this.feedbackYesNoButtonsMap.get(this.genCorrectFeedbackKey());
		if (correctSelection.isYesButtonSelected()) {
			return new DPUserFeedback(DPUserFeedbackType.CORRECT, node);
		}
		
		// This step should not be executed
		YesNoButtonPair wrongPathSelection = this.feedbackYesNoButtonsMap.get(this.genWrongPathFeedbackKey());
		if (wrongPathSelection.isNoButtonSelected()) {
			return new DPUserFeedback(DPUserFeedbackType.WRONG_PATH, node);
		}
		
		// This step contain wrong variable
		DPUserFeedback dpUserFeedback = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, node);
		for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
			final UserFeedback feedback = entry.getKey();
			final YesNoButtonPair buttonPair = entry.getValue();
			
			if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
				if (buttonPair.isYesButtonSelected()) {
					dpUserFeedback.addCorrectVar(feedback.getOption().getReadVar());
				} else if (buttonPair.isNoButtonSelected()) {
					dpUserFeedback.addWrongVar(feedback.getOption().getReadVar());
				}
			}			
		}
		
		if (dpUserFeedback.getWrongVars().isEmpty()) {
			throw new RuntimeException(Log.genMsg(getClass(), "Invalid user feedback"));
		}
		return dpUserFeedback;
	}
	
	/**
	 * The selection is valid when <br/>
	 * 1. There is at least one wrong feedback (root cause, wrong path, wrong variable) or <br/>
	 * 2. User indicate that the step is correct
	 * @return True if the selection is valid
	 */
	public boolean isValidSelection() {
		for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
			final UserFeedback feedback = entry.getKey();
			final YesNoButtonPair buttonPair = entry.getValue();
			if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE) && buttonPair.isYesButtonSelected()) {
				return true;
			}
			if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH) && buttonPair.isNoButtonSelected()) {
				return true;
			}
			if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE) && buttonPair.isNoButtonSelected()) {
				return true;
			}
			if (feedback.getFeedbackType().equals(UserFeedback.CORRECT) && buttonPair.isYesButtonSelected()) {
				return true;
			}
		}
		return false;
	}
	
	public void checkButtonBasedOnFeedback(final DPUserFeedback feedback) {
		switch (feedback.getType()) {
		case CORRECT:
			/*
			 * When feedback is correct
			 * 1. This step is not root cause
			 * 2. This step should be executed
			 * 3. All variables are correct
			 */
			for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
				final UserFeedback feedbackKey = entry.getKey();
				final YesNoButtonPair pair = entry.getValue();
				if (feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
					pair.checkNoButton();
				} else {
					pair.checkYesButton();
				}
			}
			break;
		case ROOT_CAUSE:
			/*
			 * When feedback is root cause
			 * 1. This step is not correct
			 * 2. Unclear with all other feedback
			 */
			for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
				final UserFeedback feedbackKey = entry.getKey();
				final YesNoButtonPair pair = entry.getValue();
				if (feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
					pair.checkYesButton();
				} else if (feedbackKey.getFeedbackType().equals(UserFeedback.CORRECT)) {
					pair.checkNoButton();
				} else {
					pair.uncheckBothButton();
				}
				
			}
			break;
		case WRONG_PATH:
			/*
			 * When feedback is wrong path
			 * 1. This step is not root cause
			 * 2. This step is not correct
			 * 3. Unclear with variables
			 */
			for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
				final UserFeedback feedbackKey = entry.getKey();
				final YesNoButtonPair pair = entry.getValue();
				if (feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE) || feedbackKey.getFeedbackType().equals(UserFeedback.CORRECT)) {
					pair.checkNoButton();
				} else if (feedbackKey.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
					pair.checkNoButton();
				} else {
					pair.uncheckBothButton();
				}
				
			}
			break;
		case WRONG_VARIABLE:
			/*
			 * When feedback is wrong variable
			 * 1. This step is not root cause
			 * 2. This step is in correct branch
			 * 3. This step is not correct
			 */
			for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
				final UserFeedback feedbackKey = entry.getKey();
				final YesNoButtonPair pair = entry.getValue();
				if (feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE) ||
					feedbackKey.getFeedbackType().equals(UserFeedback.CORRECT)) {
					pair.checkNoButton();
				} else if (feedbackKey.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
					pair.checkYesButton();
				} else {
					final VarValue var = feedbackKey.getOption().getReadVar();
					if (feedback.getWrongVars().contains(var)) {
						pair.checkNoButton();;
					} else if (feedback.getCorrectVars().contains(var)) {
						pair.checkYesButton();
					}
				}
			}
			break;
		default:
			throw new RuntimeException(Log.genMsg(getClass(), "Unhandled feedback type: " + feedback.getType().name()));
		}
	}
	
	public void select(final UserFeedback userFeedback, final boolean isYesButton) {
		final String feedbackType = userFeedback.getFeedbackType();
		
		if (feedbackType.equals(UserFeedback.ROOTCAUSE)) {
			// If user select root cause, then uncheck all the other option
			YesNoButtonPair pair = this.feedbackYesNoButtonsMap.get(userFeedback);
			if (pair.isYesButtonSelected()) {				
				for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
					final UserFeedback feedbackKey = entry.getKey();
					final YesNoButtonPair buttonPair = entry.getValue();
					if (!feedbackKey.getFeedbackType().equals(feedbackType)) {
						buttonPair.uncheckBothButton();
					}
				}
			}
		} else if (feedbackType.equals(UserFeedback.WRONG_PATH)) {
			// If user select wrong path, no matter it is wrong or correct, that mean this step is not root cause
			YesNoButtonPair pair = this.feedbackYesNoButtonsMap.get(userFeedback);
			this.feedbackYesNoButtonsMap.get(this.genRootCuaseFeedbackKey()).checkNoButton();
			if (pair.isNoButtonSelected()) {
				// If user say it is wrong path, then this step is wrong and all variable is unclear
				for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
					final UserFeedback feedbackKey = entry.getKey();
					final YesNoButtonPair buttonPair = entry.getValue();
					if (feedbackKey.getFeedbackType().equals(UserFeedback.CORRECT) || feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
						buttonPair.checkNoButton();
					} else if (!feedbackKey.getFeedbackType().equals(feedbackType)) {
						buttonPair.uncheckBothButton();
					}
				}				
			}
		} else if (feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			// If user select wrong variable, that mean this step is not root cause and should be executed
			YesNoButtonPair pair = this.feedbackYesNoButtonsMap.get(userFeedback);
			if (pair.isNoButtonSelected()) {				
				for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
					final UserFeedback feedbackKey = entry.getKey();
					final YesNoButtonPair buttonPair = entry.getValue();
					if (feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE) ||
							feedbackKey.getFeedbackType().equals(UserFeedback.CORRECT)) {
						buttonPair.checkNoButton();
					} else if (feedbackKey.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
						buttonPair.checkYesButton();
					} else if (!feedbackKey.getFeedbackType().equals(feedbackType)) {
						buttonPair.uncheckBothButton();
					}
				}
			}
		} else if (feedbackType.equals(UserFeedback.CORRECT) && this.feedbackYesNoButtonsMap.get(userFeedback).isYesButtonSelected()) {
			// If user select correct, then mean
			// 1. This step is not root cause
			// 2. This step should be executed
			// 3. All variables is correct
			for (Map.Entry<UserFeedback, YesNoButtonPair> entry : this.feedbackYesNoButtonsMap.entrySet()) {
				final UserFeedback feedbackKey = entry.getKey();
				final YesNoButtonPair buttonPair = entry.getValue();
				if (feedbackKey.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
					buttonPair.checkNoButton();
				} else if (!feedbackKey.getFeedbackType().equals(feedbackType)) {
					buttonPair.checkYesButton();
				}
			}
		}
		
		// Handle the feedback itself
		YesNoButtonPair buttonPair = this.feedbackYesNoButtonsMap.get(userFeedback);
		buttonPair.handleCheck(isYesButton);			
	}
	
	protected UserFeedback genCorrectFeedbackKey() {
		return new UserFeedback(UserFeedback.CORRECT);
	}
	
	protected UserFeedback genRootCuaseFeedbackKey() {
		return new UserFeedback(UserFeedback.ROOTCAUSE);
	}
	
	protected UserFeedback genWrongPathFeedbackKey() {
		return new UserFeedback(UserFeedback.WRONG_PATH);
	}
}
