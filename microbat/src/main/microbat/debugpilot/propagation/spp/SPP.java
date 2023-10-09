package microbat.debugpilot.propagation.spp;

import java.util.Collection;
import java.util.List;

import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public abstract class SPP implements ProbabilityPropagator {

	protected final Trace trace;
	protected final List<TraceNode> slicedTrace;
	protected final Collection<DPUserFeedback> feedbackRecords;
	
	public SPP(final PropagatorSettings settings) {
		this(settings.getTrace(), settings.getSlicedTrace(), settings.getFeedbacks());
	}
	
	public SPP(final Trace trace, final List<TraceNode> slicedTrace, final Collection<DPUserFeedback> feedbackRecords) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
		this.feedbackRecords = feedbackRecords;
	}
}
