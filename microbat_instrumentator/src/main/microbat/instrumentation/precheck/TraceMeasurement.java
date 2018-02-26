package microbat.instrumentation.precheck;

import java.util.HashMap;
import java.util.Map;

import microbat.model.ClassLocation;

public class TraceMeasurement {
	private static Map<Long, TraceMeasurement> rtStores = new HashMap<>();
	private static long mainThreadId = -1;
	private TraceInfo trace = new TraceInfo(stepLimit);
	private static int stepLimit = Integer.MAX_VALUE;

	public void _hitLine(int line, String className, String methodSignature) {
		if (trace.isOverLong()) {
			throw new RuntimeException("Trace is overlong");
		}
		try {
			ClassLocation lastStep = trace.getLastStep();
			if (lastStep != null && lastStep.getClassCanonicalName().equals(className) && 
					lastStep.getLineNumber() == line) {
				return;
			}
			/* add new step */
			ClassLocation newStep = new ClassLocation(className, methodSignature, line);
			trace.addStep(newStep);
		} catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}
	
	public synchronized static TraceMeasurement _getTracer(String className, String methodSig) {
		long threadId = Thread.currentThread().getId();
		if (mainThreadId < 0) {
			mainThreadId = threadId;
		}
		TraceMeasurement instance = getInstance(threadId);
		return instance;
	}
	
	public synchronized static int getThreadNumber() {
		return rtStores.size();
	}
	
	public static TraceMeasurement getMainThreadInstance() {
		return getInstance(mainThreadId);
	}
	
	private static TraceMeasurement getInstance(long threadId) {
		TraceMeasurement store = rtStores.get(threadId);
		if (store == null) {
			store = new TraceMeasurement();
			rtStores.put(threadId, store);
		}
		return store;
	}
	
	public synchronized static PrecheckInfo getPrecheckInfo() {
		return new PrecheckInfo(rtStores.size(), getMainThreadInstance().trace);
	}
	
	public static void setStepLimit(int stepLimit) {
		TraceMeasurement.stepLimit = stepLimit;
	}
}
