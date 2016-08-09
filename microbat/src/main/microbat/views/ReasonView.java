package microbat.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import microbat.recommendation.StepRecommender;

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
