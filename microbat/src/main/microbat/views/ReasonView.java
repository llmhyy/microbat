package microbat.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import microbat.model.trace.TraceNode;
import microbat.recommendation.StepRecommender;
import microbat.recommendation.UserFeedback;

public class ReasonView extends ViewPart {

	private StyledText reasonText;
	
	public ReasonView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, true);
		parent.setLayout(parentLayout);
		
		createReasonGroup(parent);
	}
	
	public void refresh(StepRecommender recommender){
		ReasonGenerator reasonGenerator = new ReasonGenerator();
		String reason  = reasonGenerator.generateReason(recommender);
		reasonText.setText("** " + reason);
	}

	public void refresh(final TraceNode node, final UserFeedback userFeedback) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Trace Node: " + node.getOrder());
		stringBuilder.append('\n');
		stringBuilder.append(this.feedbackToString(userFeedback));
		stringBuilder.append('\n');
		
		if (!node.reason.isEmpty()) {
			stringBuilder.append('\n');
			stringBuilder.append("Reason:\n");
			stringBuilder.append(node.reason);
		}
		
		this.reasonText.setText(stringBuilder.toString());
		
	}
	
	public String feedbackToString(final UserFeedback feedback) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Prediction: ");
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			stringBuilder.append("This step is correct");
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			stringBuilder.append("The variable " + feedback.getOption().getReadVar().getVarName() + " is wrong"); 
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH) ) {
			stringBuilder.append("This step should not be executed");
		} else if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
			stringBuilder.append("This step is the root cause of the bug");
		} else if (feedback.getFeedbackType().equals(UserFeedback.UNCLEAR)) {
			stringBuilder.append("DebugPilot is not certain about this step ");
		}
		return stringBuilder.toString();
	}
	
	private void createReasonGroup(Composite parent) {
		Group feedbackGroup = new Group(parent, SWT.NONE);
		feedbackGroup.setText("Explanation");
		feedbackGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout gl = new GridLayout(1, true);
		gl.makeColumnsEqualWidth = false;
		gl.marginWidth = 1;
		feedbackGroup.setLayout(gl);
		
		Label reasonLabel = new Label(feedbackGroup, SWT.None);
		reasonLabel.setText("Reason: ");
		reasonLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		reasonText = new StyledText(feedbackGroup, SWT.WRAP | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		reasonText.setAlwaysShowScrollBars(true);
		ReasonGenerator reasonGenerator = new ReasonGenerator();
		String reason  = reasonGenerator.generateReason(null);
		reasonText.setText("** " + reason);
		GridData gData = new GridData(SWT.FILL, SWT.FILL, true, true);
		reasonText.setLayoutData(gData);
	}

	@Override
	public void setFocus() {

	}

}
