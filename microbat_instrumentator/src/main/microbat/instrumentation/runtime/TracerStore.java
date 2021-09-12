package microbat.instrumentation.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LLT
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 * [TO AVOID RECURSIVE LOOP IN GET_TRACER!!]
 */
public abstract class TracerStore<T extends ExecutionTracer> {
	public static final int INVALID_THREAD_ID = -1;
	private Map<Long, T> rtStore = new HashMap<>();
	protected long mainThreadId = INVALID_THREAD_ID;
	protected transient int lastUsedIdx = INVALID_THREAD_ID;
	
	/* threadId must be valid */
	public synchronized T get(long threadId) {
		if (rtStore.containsKey(threadId)) {
			return rtStore.get(threadId);
		}
		T tracer = initTracer(threadId);
		rtStore.put(threadId, tracer);
		return tracer;
	}
	
	protected abstract T initTracer(long threadId);

	public void setMainThreadId(long mainThreadId) {
		this.mainThreadId = mainThreadId;
		get(mainThreadId).setMain(true);
	}

	public T getMainThreadTracer() {
		return get(mainThreadId);
	}
	
	public long getMainThreadId() {
		return mainThreadId;
	}
	
	public List<IExecutionTracer> getAllThreadTracer() {
		return new ArrayList<IExecutionTracer>(rtStore.values());
	}
}
