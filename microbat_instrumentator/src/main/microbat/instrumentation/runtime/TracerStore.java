package microbat.instrumentation.runtime;

/**
 * @author LLT
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 * [TO AVOID RECURSIVE LOOP IN GET_TRACER!!]
 */
public class TracerStore {
	private ExecutionTracer[] rtStore = new ExecutionTracer[10];
	private long mainThreadId = -1;
	private int lastUsedIdx = -1;
	
	/* threadId must be valid */
	public synchronized ExecutionTracer get(long threadId) {
		if (threadId != mainThreadId) {
			return null; // for now, only recording trace for main thread.
		}
		for (int i = 0; i < lastUsedIdx; i++) {
			ExecutionTracer tracer = rtStore[i];
			if (tracer.getThreadId() == threadId) {
				return tracer;
			}
		}
		ExecutionTracer tracer = new ExecutionTracer(threadId);
		rtStore[++lastUsedIdx] = tracer;
		return tracer;
	}
	
	public IExecutionTracer getMainThreadTracer() {
		return get(mainThreadId);
	}
	
	public void setMainThreadId(long mainThreadId) {
		this.mainThreadId = mainThreadId;
	}
}
