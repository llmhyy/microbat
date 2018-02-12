package microbat.instrumentation.trace.data;

import microbat.model.trace.TraceNode;

public class OnWorkingMethod {
	private TraceNode currentNode;
	private boolean exclusive; // current method is in exclude list.

	public OnWorkingMethod(TraceNode currentNode, boolean exclusive) {
		this.currentNode = currentNode;
		this.exclusive = exclusive;
	}

	public TraceNode getCurrentNode() {
		return currentNode;
	}

	public boolean isExclusive() {
		return exclusive;
	}


}
