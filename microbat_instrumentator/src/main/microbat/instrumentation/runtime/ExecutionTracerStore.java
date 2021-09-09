package microbat.instrumentation.runtime;

public class ExecutionTracerStore extends TracerStore<ExecutionTracer> {

	private TracingContext ctx;

	public ExecutionTracerStore(TracingContext ctx) {
		this.ctx = ctx;
	}

	@Override
	protected ExecutionTracer initTracer(long threadId) {
		return new ExecutionTracer(threadId, this.ctx);
	}

}
