package microbat.recommendation;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public abstract class DetailInspector {
	protected InspectingRange inspectingRange;

	public InspectingRange getInspectingRange() {
		return inspectingRange;
	}

	public void setInspectingRange(InspectingRange inspectingRange) {
		this.inspectingRange = inspectingRange;
	}

	public abstract TraceNode recommendDetailNode(TraceNode currentNode, Trace trace, VarValue wrongValue);
	public abstract DetailInspector clone();

}
