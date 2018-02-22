package microbat.instrumentation.runtime;

import java.util.Stack;

import microbat.model.trace.TraceNode;

public class MethodCallStack extends Stack<OnWorkingMethod> {
	private static final long serialVersionUID = 1L;

	public OnWorkingMethod safePop() {
		if (size() != 0) {
			return pop();
		}
		return null;
	}

	public void push(TraceNode node, boolean exclusive) {
		super.push(new OnWorkingMethod(node, exclusive));
	}
	
}
