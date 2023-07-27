package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;

public class ProbInferRootCauseLocator extends AbstractRootCauseLocator {

	public ProbInferRootCauseLocator(List<TraceNode> sliceTrace, Collection<NodeFeedbacksPair> feedbacks, TraceNode outputNode) {
		super(sliceTrace, feedbacks, outputNode);
	}

	@Override
	public TraceNode locateRootCause() {
		return this.slicedTrace.stream()
				.filter(node -> !node.equals(this.outputNode))
				.filter(node -> !this.isFeedbackGivenTo(node))
				.min(Comparator.comparingDouble(TraceNode::getProbability))
				.orElse(null);
	}

}
