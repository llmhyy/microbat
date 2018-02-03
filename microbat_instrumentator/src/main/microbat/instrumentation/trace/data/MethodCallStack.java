package microbat.instrumentation.trace.data;

import java.util.Stack;

import microbat.model.BreakPoint;
import microbat.model.trace.TraceNode;

public class MethodCallStack extends Stack<OnWorkingMethod> {
	private static final long serialVersionUID = 1L;

	public OnWorkingMethod safePop() {
		if (size() != 0) {
			return pop();
		}
		return null;
	}

	public void push(TraceNode currentNode, BreakPoint methodEntry, boolean exclusive, InvokingTrack invokeTrack) {
		super.push(new OnWorkingMethod(currentNode, methodEntry, exclusive, invokeTrack));
	}
	
}
