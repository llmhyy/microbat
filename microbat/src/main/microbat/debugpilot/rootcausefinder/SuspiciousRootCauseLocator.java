package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import microbat.debugpilot.settings.RootCauseLocatorSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.TraceNode;

public class SuspiciousRootCauseLocator extends AbstractRootCauseLocator {

	public SuspiciousRootCauseLocator(final RootCauseLocatorSettings settings) {
		this(settings.getSliceTrace(), settings.getFeedbacks(), settings.getOutputNode());
	}
	
	public SuspiciousRootCauseLocator(List<TraceNode> sliceTrace, Collection<DPUserFeedback> feedbacks, TraceNode outputNode) {
		super(sliceTrace, feedbacks, outputNode);
	}
	
	@Override
	public TraceNode locateRootCause() {
		Optional<TraceNode> rootCauseOptinal = this.slicedTrace.stream()
                .max((s1, s2) -> Double.compare(s1.getSuspicousness(), s2.getSuspicousness()));
		
		if (rootCauseOptinal.isEmpty()) {
			throw new RuntimeException("Cannot find root cause by suspicious");
		} else {
			return rootCauseOptinal.get();
		}
	}

}
