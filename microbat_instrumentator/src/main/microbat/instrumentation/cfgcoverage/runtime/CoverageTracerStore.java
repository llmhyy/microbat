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
	protected int lastUsedIdx = INVALID_THREAD_ID;
	
	/* threadId must be valid */
	public synchronized CoverageTracer get(long threadId, int testCaseIdx) {
		for (int i = 0; i <= lastUsedIdx; i++) {
			CoverageTracer tracer = rtStore[i];
			if (tracer.getThreadId() == threadId && tracer.getTestIdx() == testCaseIdx) {
				return tracer;
			}
		}
		return null;
	}
	
	public CoverageTracer create(long threadId, int testCaseIdx) {
		CoverageTracer tracer = new CoverageTracer(threadId, testCaseIdx);
		rtStore[++lastUsedIdx] = tracer;
		return tracer;
	}
}
