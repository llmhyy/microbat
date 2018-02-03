package microbat.instrumentation.trace.data;

import microbat.model.BreakPoint;
import microbat.model.trace.TraceNode;

public class OnWorkingMethod {
	private TraceNode currentNode;
	private BreakPoint methodEntry;
	private boolean exclusive; // current method is in exclude list.
	private InvokingTrack invokeTrack;

	public OnWorkingMethod(TraceNode currentNode, BreakPoint methodEntry, boolean exclusive,
			InvokingTrack invokeTrack) {
		this.currentNode = currentNode;
		this.methodEntry = methodEntry;
		this.exclusive = exclusive;
		this.invokeTrack = invokeTrack;
	}

	public TraceNode getCurrentNode() {
		return currentNode;
	}

	public BreakPoint getMethodEntry() {
		return methodEntry;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public InvokingTrack getInvokeTrack() {
		return invokeTrack;
	}

}
