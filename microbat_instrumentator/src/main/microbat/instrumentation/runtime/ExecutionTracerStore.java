package microbat.instrumentation.runtime;

public class ExecutionTracerStore extends TracerStore<ExecutionTracer> {
	private int threshold = 1000;

	@Override
	protected ExecutionTracer initTracer(long threadId) {
		return new ExecutionTracer(threadId);
	}

}
