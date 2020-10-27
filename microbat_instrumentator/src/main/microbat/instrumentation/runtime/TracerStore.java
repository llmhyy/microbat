package microbat.instrumentation.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 * [TO AVOID RECURSIVE LOOP IN GET_TRACER!!]
 */
public abstract class TracerStore<T extends ITracer> {
	public static final int INVALID_THREAD_ID = -1;
	protected ITracer[] rtStore = new ITracer[10];
	protected long mainThreadId = INVALID_THREAD_ID;
	protected transient int lastUsedIdx = INVALID_THREAD_ID;
	
	/* threadId must be valid */
	@SuppressWarnings("unchecked")
	public synchronized T get(long threadId) {
		// FIXME -mutithread LINYUN [2]
		// LLT: this is where we disable recording other threads not the main one
//		if (threadId != mainThreadId) {
//			return null; // for now, only recording trace for main thread.
//		}
		
		int i = 0;
		while(i <= lastUsedIdx) {
			ITracer tracer = rtStore[i];
			if (tracer.getThreadId() == threadId) {
				return (T) tracer;
			}
			i++;
		}
		T tracer = initTracer(threadId);
		rtStore[++lastUsedIdx] = tracer;
		return tracer;
	}
	
	protected abstract T initTracer(long threadId);

	public void setMainThreadId(long mainThreadId) {
		this.mainThreadId = mainThreadId;
	}

	public T getMainThreadTracer() {
		return get(mainThreadId);
	}
	
	public long getMainThreadId() {
		return mainThreadId;
	}
	
	public List<IExecutionTracer> getAllThreadTracer() {
		List<IExecutionTracer> traces = new ArrayList<>();
		for(int i=0; i<rtStore.length; i++) {
			if(rtStore[i] != null) {
				traces.add((IExecutionTracer) rtStore[i]);
			}
		}
		
		return traces;
	}
}
