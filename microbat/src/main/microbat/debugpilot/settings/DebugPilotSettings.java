package microbat.debugpilot.settings;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class DebugPilotSettings {

	protected PathFinderSettings pathFinderSettings = new PathFinderSettings();
	protected PropagatorSettings propagatorSettings = new PropagatorSettings();
	protected RootCauseLocatorSettings rootCauseLocatorSettings = new RootCauseLocatorSettings();

	public DebugPilotSettings() {

	}

	public List<DPUserFeedback> getFeedbacks() {
		return this.propagatorSettings.getFeedbacks();
	}

	public TraceNode getOutputNode() {
		return this.rootCauseLocatorSettings.getOutputNode();
	}

	public PathFinderSettings getPathFinderSettings() {
		return pathFinderSettings;
	}

	public PropagatorSettings getPropagatorSettings() {
		return propagatorSettings;
	}

	public RootCauseLocatorSettings getRootCauseLocatorSettings() {
		return rootCauseLocatorSettings;
	}

	public Trace getTrace() {
		return this.propagatorSettings.getTrace();
	}

	public void setCorrectVars(Set<VarValue> correctVars) {
		this.propagatorSettings.setCorrectVars(correctVars);
	}

	public void setFeedbackRecords(Collection<DPUserFeedback> feedbackRecords) {
		this.propagatorSettings.setFeedbacks(feedbackRecords);
		this.rootCauseLocatorSettings.setFeedbacks(feedbackRecords);
	}

	public void setOutputNode(TraceNode outputNode) {
//		this.outputNode = outputNode;
		this.rootCauseLocatorSettings.setOutputNode(outputNode);
	}

	public void setPathFinderSettings(PathFinderSettings pathFinderSettings) {
		this.pathFinderSettings = pathFinderSettings;
	}

	public void setPropagatorSettings(PropagatorSettings propagatorSettings) {
		this.propagatorSettings = propagatorSettings;
	}

	public void setRootCauseLocatorSettings(RootCauseLocatorSettings rootCauseLocatorSettings) {
		this.rootCauseLocatorSettings = rootCauseLocatorSettings;
	}

	public void setSlicedTrace(List<TraceNode> slicedTrace) {
		this.propagatorSettings.setSlicedTrace(slicedTrace);
		this.pathFinderSettings.setSlicedTrace(slicedTrace);
		this.rootCauseLocatorSettings.setSliceTrace(slicedTrace);
	}
	
	public void setTrace(Trace trace) {
//		this.trace = trace;
		this.propagatorSettings.setTrace(trace);
		this.pathFinderSettings.setTrace(trace);
	}
	
	public void setWrongVars(Set<VarValue> wrongVars) {
		this.propagatorSettings.setWrongVars(wrongVars);
	}
	
	
}
