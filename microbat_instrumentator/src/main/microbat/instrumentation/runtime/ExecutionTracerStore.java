package microbat.instrumentation.runtime;

public class ExecutionTracerStore extends TracerStore<ExecutionTracer> {

	@Override
	protected ExecutionTracer initTracer(long threadId) {
		return new ExecutionTracer(threadId);
	}

}
