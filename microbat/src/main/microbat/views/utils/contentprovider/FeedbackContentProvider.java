package microbat.views.utils.contentprovider;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import microbat.recommendation.UserFeedback;

public class FeedbackContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof UserFeedback[] feedbacks) {
			return feedbacks;
		}
		return null;
	}



}
