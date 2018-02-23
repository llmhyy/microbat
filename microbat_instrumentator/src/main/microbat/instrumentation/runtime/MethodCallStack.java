package microbat.instrumentation.runtime;

import java.util.Stack;

import microbat.model.trace.TraceNode;

public class MethodCallStack extends Stack<TraceNode> {
	private static final long serialVersionUID = 1L;

	public TraceNode safePop() {
		if (size() != 0) {
			return pop();
		}
		return null;
	}

	public TraceNode push(TraceNode node) {
		return super.push(node);
	}
	
	@Override
	public synchronized TraceNode peek() {
		if (isEmpty()) {
			return null;
		}
		return super.peek();
	}
}
