package microbat.views.utils.lableprovider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;
import microbat.util.TraceUtil;

public class FeedbackLabelProvider implements ITableLabelProvider {

	protected final TraceNode currentNode;
	protected final Trace trace;
	
	public FeedbackLabelProvider(final TraceNode currentNode, final Trace trace) {
		this.currentNode = currentNode;
		this.trace = trace;
	}
	
	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof UserFeedback userFeedback) {
			switch (columnIndex) {
			case 0:
				return this.genFeedbackType(userFeedback);
			case 1:
				VarValue wrongVar = null;
				if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
					wrongVar = userFeedback.getOption().getReadVar();
					return wrongVar.getVarName();
				}
				return "-";
			case 2:
				if (userFeedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
					return userFeedback.getOption().getReadVar().getManifestationValue();					
				} else {
					return "-";
				}
			case 3:
				final TraceNode nextNode = TraceUtil.findNextNode(this.currentNode, userFeedback, this.trace);
				return nextNode == null ? "-" : String.valueOf(nextNode.getOrder());
			default:
				return null;
			}
		}
		return null;
	}
	
	protected String genFeedbackType(final UserFeedback feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			return "CORRECT";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			return "WRONG_BRANCH";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			return "WRONG_VARIABLE";
		} else if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
			return "ROOT_CAUSE";
		} else {
			return null;
		}
	}
	
	protected String genDescription(final UserFeedback feedback) {
		if (feedback.getFeedbackType().equals(UserFeedback.CORRECT)) {
			return "This step is correct";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_PATH)) {
			return "This step should not be executed";
		} else if (feedback.getFeedbackType().equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
			return "This variable is wrong";
		} else if (feedback.getFeedbackType().equals(UserFeedback.ROOTCAUSE)) {
			return "This step is the root cause";
		} else {
			return null;
		}
	}

}
