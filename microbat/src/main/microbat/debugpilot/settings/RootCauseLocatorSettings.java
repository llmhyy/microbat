package microbat.debugpilot.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.rootcausefinder.RootCauseLocatorType;
import microbat.model.trace.TraceNode;

public class RootCauseLocatorSettings {
	
	public static final RootCauseLocatorType DEFAULT_ROOT_CAUSE_LOCATOR_TYPE = RootCauseLocatorType.SPP;
	
	protected RootCauseLocatorType rootCauseLocatorType = RootCauseLocatorSettings.DEFAULT_ROOT_CAUSE_LOCATOR_TYPE;
	protected List<TraceNode> sliceTrace = null;
	protected Collection<NodeFeedbacksPair> feedbacks = new ArrayList<>();
	protected TraceNode outputNode = null; 
	
	public RootCauseLocatorSettings() {
		
	}
	
	public RootCauseLocatorSettings(RootCauseLocatorType rootCauseLocatorType, List<TraceNode> sliceTrace,
			Collection<NodeFeedbacksPair> feedbacks, TraceNode outputNode) {
		this.rootCauseLocatorType = rootCauseLocatorType;
		this.sliceTrace = sliceTrace;
		this.feedbacks = feedbacks;
		this.outputNode = outputNode;
	}

	public RootCauseLocatorType getRootCauseLocatorType() {
		return rootCauseLocatorType;
	}

	public void setRootCauseLocatorType(RootCauseLocatorType rootCauseLocatorType) {
		this.rootCauseLocatorType = rootCauseLocatorType;
	}

	public List<TraceNode> getSliceTrace() {
		return sliceTrace;
	}

	public void setSliceTrace(List<TraceNode> sliceTrace) {
		this.sliceTrace = sliceTrace;
	}

	public Collection<NodeFeedbacksPair> getFeedbacks() {
		return feedbacks;
	}

	public void setFeedbacks(Collection<NodeFeedbacksPair> feedbacks) {
		this.feedbacks.clear();
		this.feedbacks.addAll(feedbacks);
	}

	public TraceNode getOutputNode() {
		return outputNode;
	}

	public void setOutputNode(TraceNode outputNode) {
		this.outputNode = outputNode;
	}
	
	
	
}
