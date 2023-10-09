package microbat.views.utils.manager;

import org.eclipse.swt.widgets.Button;

import microbat.log.Log;

public class YesNoButtonPair {
	protected Button yesButton = null;
	protected Button noButton = null;
	
	public YesNoButtonPair() {
		
	}
	
	public YesNoButtonPair(final Button yesButton, final Button noButton) {
		this.yesButton = yesButton;
		this.noButton = noButton;
	}

	public void setYesButton(Button yesButton) {
		this.yesButton = yesButton;
	}

	public void setNoButton(Button noButton) {
		this.noButton = noButton;
	}

	public Button getYesButton() {
		return yesButton;
	}

	public Button getNoButton() {
		return noButton;
	}
	
	public void dispose() {
		if (this.yesButton != null) {
			this.yesButton.dispose();
		}
		if (this.noButton != null) {
			this.noButton.dispose();
		}
	}
	
	public void handleCheck(final boolean isYesButton) {
		if (!this.isValid()) {
			throw new RuntimeException(Log.genMsg(getClass(), "Yes No Button Pair is not valid"));
		}
		if (isYesButton) {
			// If yes button is checked, then no button should be unchecked
			boolean isYesButtonChecked = this.yesButton.getSelection();
			if (isYesButtonChecked) {
				this.noButton.setSelection(false);
			}
		} else {
			// If no button is checked, then yes button should be unchecked
			boolean isNoButtonChecked = this.noButton.getSelection();
			if (isNoButtonChecked) {
				this.yesButton.setSelection(false);
			}
		}
	}

	public void checkYesButton() {
		if (!this.isValid()) {
			throw new RuntimeException(Log.genMsg(getClass(), "Yes No Button Pair is not valid"));
		}
		this.yesButton.setSelection(true);
		this.noButton.setSelection(false);
	}
	
	public void checkNoButton() {
		if (!this.isValid()) {
			throw new RuntimeException(Log.genMsg(getClass(), "Yes No Button Pair is not valid"));
		}
		this.yesButton.setSelection(false);
		this.noButton.setSelection(true);
	}
	
	public void uncheckYesButton() {
		if (!this.isValid()) {
			throw new RuntimeException(Log.genMsg(getClass(), "Yes No Button Pair is not valid"));
		}
		this.yesButton.setSelection(false);
	}
	
	public void uncheckNoButton() {
		if (!this.isValid()) {
			throw new RuntimeException(Log.genMsg(getClass(), "Yes No Button Pair is not valid"));
		}
		this.noButton.setSelection(false);
	}
	
	public void uncheckBothButton() {
		this.uncheckYesButton();
		this.uncheckNoButton();
	}
	
	public boolean isYesButtonSelected() {
		return this.yesButton.getSelection();
	}
	
	public boolean isNoButtonSelected() {
		return this.noButton.getSelection();
	}
	
	public boolean isValid() {
		return this.yesButton != null && this.noButton != null;
	}
}
