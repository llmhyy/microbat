package microbat.instrumentation.precheck;

import java.util.HashMap;
import java.util.Map;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.runtime.TracingState;
import microbat.model.ClassLocation;

public class TraceMeasurement {
	private static Map<Long, TraceMeasurement> rtStores = new HashMap<>();
	private static long mainThreadId = -1;
	private TraceInfo trace = new TraceInfo(stepLimit);
	public static int stepLimit = Integer.MAX_VALUE;
	private static int maxSteps = stepLimit;
	private static TracingState state = TracingState.INIT;
	private long threadId;

	public void _hitLine(int line, String className, String methodSignature) {
		if (state != TracingState.RECORDING) {
			return;
		}
		if (trace.getStepTotal() > maxSteps) {
			Agent._exitProgram("fail;Trace is over long!");
		}
		try {
			ClassLocation lastStep = trace.getLastStep();
			if (lastStep != null && lastStep.getClassCanonicalName().equals(className)
					&& lastStep.getLineNumber() == line) {
				return;
			}
			/* add new step */
			ClassLocation newStep = new ClassLocation(className, methodSignature, line);
			trace.addStep(newStep);
		} catch (Throwable t) {
			AgentLogger.info("TraceMesurement error: " + t.getMessage());
			AgentLogger.error(t);
		}
	}
	
	public synchronized static TraceMeasurement _getTracer(String className, String methodSig, int methodStartLine) {
		long threadId = Thread.currentThread().getId();
		if (mainThreadId < 0) {
			mainThreadId = threadId;
		}
		TraceMeasurement instance = getInstance(threadId);
		instance._hitLine(methodStartLine, className, methodSig);
		return instance;
	}
	
	public static void _start() {
		state = TracingState.RECORDING;
	}
	
	public static int getThreadNumber() {
		return rtStores.size();
	}
	
	public static TraceMeasurement getMainThreadInstance() {
		return getInstance(mainThreadId);
	}
	
	private static TraceMeasurement getInstance(long threadId) {
		TraceMeasurement store = rtStores.get(threadId);
		if (store == null) {
			store = new TraceMeasurement();
			store.threadId = threadId;
			rtStores.put(threadId, store);
		}
		return store;
	}
	
	public static PrecheckInfo getPrecheckInfo() {
		TraceInfo total = new TraceInfo(stepLimit);
		for(Long key: rtStores.keySet()) {
			TraceInfo info = rtStores.get(key).trace;
			total.stepsTotal += info.stepsTotal;
			total.visitedLocs.addAll(info.visitedLocs);
		}
		
		return new PrecheckInfo(rtStores.size(), total);
	}
	
	public static void setStepLimit(int stepLimit) {
		if (stepLimit != AgentConstants.UNSPECIFIED_INT_VALUE) {
			TraceMeasurement.stepLimit = stepLimit;
			TraceMeasurement.maxSteps = (int) (stepLimit * 1.05);
		}
	}

	public static void shutdown() {
		state = TracingState.SHUTDOWN;
	}
}
