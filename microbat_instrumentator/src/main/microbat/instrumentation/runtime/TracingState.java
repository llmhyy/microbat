package microbat.instrumentation.runtime;

public enum TracingState {
	INIT,  // first state of tracer
	TEST_STARTED,  // testcase enter
	RECORDING,  // start recording trace
	SHUTDOWN   // tracer stopped recording
}
