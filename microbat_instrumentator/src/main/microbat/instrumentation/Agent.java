package microbat.instrumentation;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.FilterChecker;
import microbat.model.trace.Trace;
import microbat.sql.TraceRecorder;

public class Agent {

	public void startup() {
		/* init filter */
		FilterChecker.setup();
	}

	public void shutdown() throws Exception {
		/* collect trace & store */
		Trace trace = ExecutionTracer.getMainThreadStore().getTrace();
		TraceRecorder traceRecorder = new TraceRecorder();
		traceRecorder.storeTrace(trace);
	}

	public static String extrctJarPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
