package microbat.instrumentation.trace.data;

import java.util.HashMap;
import java.util.Map;

import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionTracer {
	private static Map<Long, ExecutionTracer> rtStores;

	static {
		rtStores = new HashMap<>();
	}

	private Trace trace;

	private TraceNode currentNode;
	private BreakPoint methodEntry;
	private MethodCallStack methodCallStack;

	ExecutionTracer() {
		methodCallStack = new MethodCallStack();
		AppJavaClassPath appJavaClassPath = new AppJavaClassPath();
		trace = new Trace(appJavaClassPath);
	}

	public void enterMethod(String className, String methodName) {
		methodEntry = new BreakPoint(className, null, methodName, -1);
		currentNode = null;
	}

	public void exitMethod(int line) {
		currentNode = methodCallStack.safePop();
	}

	public void hitInvoke(int line, Object invokeObj, String methodName) {
		hitLine(line);
		methodCallStack.push(currentNode);
	}

	public void hitLine(int line) {
		if (currentNode != null && currentNode.getBreakPoint().getLineNumber() == line) {
			return;
		}
		BreakPoint bkp = new BreakPoint(methodEntry.getClassCanonicalName(), null, methodEntry.getMethodName(), line);
		int order = trace.size() + 1;
		currentNode = new TraceNode(bkp, null, order, trace);
	}

	public void readField(Object refValue, Object fieldValue, int fieldIdx, String fieldTypeSign, int line) {
		hitLine(line);
	}

	public void writeField(Object refValue, Object fieldValue, int fieldIdx, String fieldTypeSign, int line) {
		hitLine(line);
	}
	
	public void writeStaticField(Object fieldValue, String refType, String fieldName, String fieldType, int line) {
		hitLine(line);
	}

	public void writeLocalVar(Object value, String varName, String varType, int line, int bcLocalVarIdx) {
		hitLine(line);
	}
	
	public void readLocalVar(Object value, String varName, String varType, int line, int bcLocalVarIdx) {
		hitLine(line);
	}
	
	public void tryTracer(Object refValue, Object fieldValue) {
		// TODO LLT: do nothing, JUST FOR TEST. TO REMOVE.
	}

	public synchronized static ExecutionTracer getTracer() {
		long threadId = Thread.currentThread().getId();
		ExecutionTracer store = rtStores.get(threadId);
		if (store == null) {
			store = new ExecutionTracer();
			rtStores.put(threadId, store);
		}
		return store;
	}
}
