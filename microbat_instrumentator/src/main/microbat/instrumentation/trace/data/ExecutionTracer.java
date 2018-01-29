package microbat.instrumentation.trace.data;

import java.util.HashMap;
import java.util.Map;

public class ExecutionTracer {
	private static Map<Long, ExecutionTracer> rtStores;
	static {
		rtStores = new HashMap<>();
	}
	
	public void enterMethod(String methodName) {
		
	}
	
	public void hitLine(int line) {
		
	}
	
	public void readField(Object value) {
		
	}
	
	public void writeField(Object value) {
		
	}
	
	public void writeLocalVar(Object value, int bclocalVarIdx) {
		
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
