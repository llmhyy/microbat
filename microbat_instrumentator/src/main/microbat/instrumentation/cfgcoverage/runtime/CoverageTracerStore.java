package microbat.instrumentation.cfgcoverage.runtime;

import java.util.Arrays;

/**
 * 
 * @author lyly
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 */
public class CoverageTracerStore {

	public static final int UNUSED_IDX = -1;
	protected CoverageTracer[] rtStore = new CoverageTracer[100];
	protected int lastUsedIdx = -1;
	
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
		ensureSize(++lastUsedIdx);
		rtStore[lastUsedIdx] = tracer;
		return tracer;
	}
	
	private void ensureSize(int idx) {
		if (idx >= rtStore.length) {
			rtStore = Arrays.copyOf(rtStore, rtStore.length + 100);
		}
	}
	
	public void clear() {
		for (int i = 0; i <= lastUsedIdx; i++) {
			rtStore[i] = null;
		}
		lastUsedIdx = UNUSED_IDX;
	}
}
