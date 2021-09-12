package microbat.instrumentation.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import microbat.instrumentation.AgentConstants;
import microbat.model.trace.Trace;
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionTrace {
	public static List<Long> stoppedThreads = new ArrayList<Long>();
	private static TracingContext ctx = new TracingContext();
	private static ExecutionTracerStore rtStore = new ExecutionTracerStore(ctx);

	public static void setExpectedSteps(int expectedSteps) {
		if (expectedSteps != AgentConstants.UNSPECIFIED_INT_VALUE) {
			ctx.expectedSteps = expectedSteps;
//			tolerantExpectedSteps = expectedSteps * 2;
		}
	}

	public static void setStepLimit(int stepLimit) {
		if (stepLimit != AgentConstants.UNSPECIFIED_INT_VALUE) {
			ctx.stepLimit = stepLimit;
		}
	}

	/**
	 * BE VERY CAREFUL WHEN MODIFYING THIS FUNCTION! TO AVOID CREATING A LOOP, DO
	 * KEEP THIS ATMOST SIMPLE, AVOID INVOKE ANY EXTERNAL LIBRARY FUNCTION, EVEN JDK
	 * INSIDE THIS BLOCK OF CODE AND ITS INVOKED METHODS.! (ONLY
	 * Thread.currentThread().getId() is exceptional used) IF NEED TO USE A LIST,MAP
	 * -> USE AN ARRAY INSTEAD!
	 */
	public synchronized static IExecutionTracer _getTracer(boolean isAppClass, String className, String methodSig,
			int methodStartLine, int methodEndLine, String paramNamesCode, String paramTypeSignsCode, Object[] params) {
		try {
			if (ctx.stateEquals(TracingState.TEST_STARTED) && isAppClass) {
				ctx.recording();
				rtStore.setMainThreadId(Thread.currentThread().getId());
			}
			if (!ctx.stateEquals(TracingState.RECORDING)) {
				return EmptyExecutionTracer.getInstance();
			}
			long threadId = Thread.currentThread().getId();
			if (ctx.lockedThreads.isUntracking(threadId)) {
				return EmptyExecutionTracer.getInstance();
			}
			ctx.lockedThreads.untrack(threadId);
			// FIXME -mutithread LINYUN [1]
			/*
			 * LLT: the corresponding tracer for a thread will be load by threadId,
			 * currently we always return null if not main thread.
			 */
			ExecutionTracer tracer = rtStore.get(threadId);
			if (tracer == null) {
				tracer = rtStore.get(threadId);
				// lockedThreads.remove(threadId);
				// return EmptyExecutionTracer.getInstance();
			}
			tracer.enterMethod(className, methodSig, methodStartLine, methodEndLine, paramTypeSignsCode, paramNamesCode,
					params);
			ctx.lockedThreads.track(threadId);
			return tracer;
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public static IExecutionTracer getMainThreadStore() {
		return rtStore.getMainThreadTracer();
	}

	public static List<IExecutionTracer> getAllThreadStore() {
		return rtStore.getAllThreadTracer();
	}

	public static synchronized IExecutionTracer getCurrentThreadStore() {
		synchronized (rtStore) {
			long threadId = Thread.currentThread().getId();
			// String threadName = Thread.currentThread().getName();
			if (ctx.lockedThreads.isUntracking(threadId)) {
				return EmptyExecutionTracer.getInstance();
			}
			IExecutionTracer tracer = rtStore.get(threadId);
			// store.setThreadName(threadName);

			if (tracer == null) {
				tracer = EmptyExecutionTracer.getInstance();
			}
			return tracer;
		}
	}

	public static synchronized void stopRecordingCurrendThread() {
		synchronized (rtStore) {
			long threadId = Thread.currentThread().getId();
			ctx.lockedThreads.untrack(threadId);
			stoppedThreads.add(threadId);
		}
	}

	public static void dispose() {
		ctx = new TracingContext();
		rtStore = new ExecutionTracerStore(ctx);
		// FIXME: move adjustVarMap to ctx if required.
//		adjustVarMap = new HashMap<>();
		HeuristicIgnoringFieldRule.clearCache();
	}

	public static void _start() {
		ctx.start();
	}

	public static boolean isShutdown() {
		return ctx.stateEquals(TracingState.SHUTDOWN);
	}

	public static void setAppJavaClassPath(AppJavaClassPath appPath) {
		ctx.setAppJavaClassPath(appPath);
	}

	public static void shutdown() {
		ctx.shutdown();
	}

	public static void setVariableLayer(int variableLayer) {
		ctx.setVariableLayer(variableLayer);
	}

	public static void setAvoidProxyToString(boolean avoidProxy) {
		ctx.setAvoidProxyToString(avoidProxy);
	}

	public static void setAppJavaClassPathOptionalTestClass(String junitClass) {
		ctx.appJavaClassPath.setOptionalTestClass(junitClass);
	}

	public static void setAppJavaClassPathOptionalTestMethod(String junitMethod) {
		ctx.appJavaClassPath.setOptionalTestMethod(junitMethod);
	}

	public static int getExpectedSteps() {
		return ctx.expectedSteps;
	}

	public static int getStepLimit() {
		return ctx.stepLimit;
	}

	public static List<Trace> getTraceList() {
		return rtStore.getAllThreadTracer().stream().map(tracer -> tracer.getTrace()).collect(Collectors.<Trace>toList());
	}
}
