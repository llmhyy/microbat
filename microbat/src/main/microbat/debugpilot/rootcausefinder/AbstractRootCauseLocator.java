package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.List;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;

public abstract class AbstractRootCauseLocator implements RootCauseLocator {
	
	protected final List<TraceNode> slicedTrace;
	protected final Collection<NodeFeedbacksPair> feedbacks;
	
	public AbstractRootCauseLocator(final List<TraceNode> sliceTrace, Collection<NodeFeedbacksPair> feedbacks) {
		this.slicedTrace = sliceTrace;
		this.feedbacks = feedbacks;
	}
	
	@Override
	public abstract TraceNode locateRootCause();
	
	protected boolean isFeedbackGivenTo(final TraceNode node) {
		return feedbacks.stream().map(feedback -> feedback.getNode()).anyMatch(feedbackNode -> feedbackNode.equals(node));
	}
}
