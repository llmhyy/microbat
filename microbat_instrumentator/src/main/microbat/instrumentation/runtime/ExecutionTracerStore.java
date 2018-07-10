package microbat.instrumentation.runtime;

public class ExecutionTracerStore extends TracerStore<ExecutionTracer> {

	public ExecutionTracer getMainThreadTracer() {
		return get(mainThreadId);
	}

	@Override
	protected ExecutionTracer initTracer(long threadId) {
		return new ExecutionTracer(threadId);
	}
}
