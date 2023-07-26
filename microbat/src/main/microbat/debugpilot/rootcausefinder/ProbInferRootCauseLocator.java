package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;

public class ProbInferRootCauseLocator extends AbstractRootCauseLocator {

	public ProbInferRootCauseLocator(List<TraceNode> sliceTrace, Collection<NodeFeedbacksPair> feedbacks) {
		super(sliceTrace, feedbacks);
	}

	@Override
	public TraceNode locateRootCause() {
		return this.slicedTrace.stream().min(Comparator.comparingDouble(TraceNode::getProbability)).orElse(null);
	}

}
