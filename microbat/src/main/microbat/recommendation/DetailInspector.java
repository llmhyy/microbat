package microbat.recommendation;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public abstract class DetailInspector {
	protected InspectingRange inspectingRange;

	public InspectingRange getInspectingRange() {
		return inspectingRange;
	}

	public void setInspectingRange(InspectingRange inspectingRange) {
		this.inspectingRange = inspectingRange;
	}

	public abstract TraceNode recommendDetailNode(TraceNode currentNode, Trace trace);
	public abstract DetailInspector clone();
}
