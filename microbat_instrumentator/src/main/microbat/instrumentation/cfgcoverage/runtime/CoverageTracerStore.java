package microbat.instrumentation.cfgcoverage.runtime;

/**
 * 
 * @author lyly
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 */
public class CoverageTracerStore {

	public static final int INVALID_THREAD_ID = -1;
	protected CoverageTracer[] rtStore = new CoverageTracer[100];
	private int curTestcaseIdx = -1;
	protected long mainThreadId = INVALID_THREAD_ID;
	protected int lastUsedIdx = INVALID_THREAD_ID;
	
	/* threadId must be valid */
	public synchronized CoverageTracer get(long threadId, int testCaseIdx) {
		if (testCaseIdx != curTestcaseIdx) {
			curTestcaseIdx = testCaseIdx;
			mainThreadId = INVALID_THREAD_ID;
		}
		if (threadId != mainThreadId) {
			return null; // only recording trace for main thread.
		}
		for (int i = 0; i <= lastUsedIdx; i++) {
			CoverageTracer tracer = rtStore[i];
			if (tracer.getThreadId() == threadId) {
				return tracer;
			}
		}
		CoverageTracer tracer = new CoverageTracer(threadId);
		rtStore[++lastUsedIdx] = tracer;
		return tracer;
	}
	

	public void setMainThreadId(long mainThreadId) {
		this.mainThreadId = mainThreadId;
	}

	public long getMainThreadId() {
		return mainThreadId;
	}
}
