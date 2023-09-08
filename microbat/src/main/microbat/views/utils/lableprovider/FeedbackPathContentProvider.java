package microbat.views.utils.lableprovider;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import microbat.debugpilot.NodeFeedbacksPair;
import microbat.debugpilot.pathfinding.FeedbackPath;

public class FeedbackPathContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FeedbackPath feedbackPath) {
			NodeFeedbacksPair[] nodeFeedbacksPairs = feedbackPath.toArray();
			Arrays.sort(nodeFeedbacksPairs, (pair1, pair2) -> {
				return pair2.getNode().getOrder() - pair1.getNode().getOrder();
			});
			return nodeFeedbacksPairs;
		}
		return null;
	}

}
