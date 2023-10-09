package microbat.views.utils.lableprovider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.log.Log;

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
		if (element instanceof DPUserFeedback nodeFeedbacksPair) {
			if (nodeFeedbacksPair.getNode().confirmed) {
				return new Color(Display.getCurrent(), 173, 255, 47);
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof DPUserFeedback userFeedback) {
			switch (columnIndex) {
			case 0:
				return String.valueOf(this.feedbacPath.indexOf(userFeedback));
			case 1:
				return String.valueOf(userFeedback.getNode().getOrder());
			case 2:
				switch (userFeedback.getType()) {
				case CORRECT:
					return "Correct";
				case ROOT_CAUSE:
					return "Root Cause";
				case WRONG_PATH:
					return "Wrong Branch";
				case WRONG_VARIABLE:
					return "Wrong Variable";
				default:
					throw new RuntimeException(Log.genMsg(getClass(), "Unhandled feedback type: " + userFeedback.getType()));
				}
			case 3:
				return userFeedback.getNode().confirmed ? "Yes" : "No";
			}
		}
		return null;
	}
	
}
