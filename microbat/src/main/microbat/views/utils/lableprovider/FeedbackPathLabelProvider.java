package microbat.views.utils.lableprovider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.recommendation.UserFeedback;

public class FeedbackPathLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

	protected final FeedbackPath feedbacPath;
	
	public FeedbackPathLabelProvider(final FeedbackPath feedbackPath) {
		this.feedbacPath = feedbackPath;
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
	public Color getBackground(Object element) {
		if (element instanceof NodeFeedbacksPair nodeFeedbacksPair) {
			if (nodeFeedbacksPair.getNode().confirmed) {
				return new Color(Display.getCurrent(), 173, 255, 47);
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof NodeFeedbacksPair userFeedbackPair) {
			switch (columnIndex) {
			case 0:
				return String.valueOf(this.feedbacPath.indexOf(userFeedbackPair));
			case 1:
				return String.valueOf(userFeedbackPair.getNode().getOrder());
			case 2:
				final String feedbackType  = userFeedbackPair.getFeedbackType();
				if (feedbackType.equals(UserFeedback.CORRECT)) {
					return "Correct";
				} else if (feedbackType.equals(UserFeedback.WRONG_PATH)) {
					return "Wrong Branch";
				} else if (feedbackType.equals(UserFeedback.WRONG_VARIABLE_VALUE)) {
					return "Wrong Variable";
				} else if (feedbackType.equals(UserFeedback.UNCLEAR)) {
					return "Unclear";
				} else if (feedbackType.equals(UserFeedback.ROOTCAUSE)) {
					return "Root Cause";
				}
			case 3:
				return userFeedbackPair.getNode().confirmed ? "Yes" : "No";
			default:
				throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			}
		}
		return null;
	}
	
}
