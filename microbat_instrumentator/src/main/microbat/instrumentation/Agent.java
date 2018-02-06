package microbat.instrumentation;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.FilterChecker;
import microbat.instrumentation.trace.data.IExecutionTracer;
import microbat.model.trace.Trace;
import microbat.sql.TraceRecorder;

public class Agent {

	public void startup() {
		/* init filter */
		FilterChecker.setup();
	}

	public void shutdown() throws Exception {
		ExecutionTracer.shutdown();
		/* collect trace & store */
		IExecutionTracer tracer = ExecutionTracer.getMainThreadStore();
		TraceRecorder traceRecorder = new TraceRecorder();
		Trace trace = ((ExecutionTracer) tracer).getTrace();
		traceRecorder.storeTrace(trace );
	}

	public static String extrctJarPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
