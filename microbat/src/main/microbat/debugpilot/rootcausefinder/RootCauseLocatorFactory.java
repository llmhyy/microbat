package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.List;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.propagation.PropagatorType;
import microbat.log.Log;
import microbat.model.trace.TraceNode;

public class RootCauseLocatorFactory {
	private RootCauseLocatorFactory() {}
	
	public static RootCauseLocator getLocator(final PropagatorType type, final List<TraceNode> sliceTrace, final Collection<NodeFeedbacksPair> feedbacks ) {
		switch (type) {
		case None:
		case ProfInfer:
			return new ProbInferRootCauseLocator(sliceTrace, feedbacks);
		case SPP_Random:
		case SPP_COST:
		case SPP_CF:
		case SPP_RL:
		case SPP_RL_TRAIN:
			return new SPPRootCauseLocator(sliceTrace, feedbacks);
		default:
			throw new IllegalArgumentException(Log.genMsg("RootCauseLocatorFactory", "Unhandled root casue locator type" + type));
		}
	}
}
