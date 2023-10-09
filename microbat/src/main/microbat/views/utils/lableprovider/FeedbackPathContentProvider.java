package microbat.views.utils.lableprovider;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import microbat.debugpilot.pathfinding.FeedbackPath;
import microbat.debugpilot.userfeedback.DPUserFeedback;

public class FeedbackPathContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FeedbackPath feedbackPath) {
			DPUserFeedback[] feedbacks = feedbackPath.toArray();
			Arrays.sort(feedbacks, (pair1, pair2) -> {
				return pair2.getNode().getOrder() - pair1.getNode().getOrder();
			});
			return feedbacks;
		}
		return null;
	}

}
