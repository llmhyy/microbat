package microbat.instrumentation.cfgcoverage.runtime;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.runtime.ITracer;
import microbat.instrumentation.runtime.MethodCallStack;
import microbat.instrumentation.runtime.TracingState;

public class CoverageTracer implements ICoverageTracer, ITracer {
	private static CoverageTracerStore rtStore = new CoverageTracerStore();
	public static CoverageSFlowGraph coverageFlowGraph;
	private long threadId;
	private TracingState state = TracingState.INIT;

	private CoverageSFNode currentNode;
	private MethodCallStack methodCallStack = new MethodCallStack();
	
	public CoverageTracer(long threadId) {
		this.threadId = threadId;
	}
	
	public void _reachNode(String methodId, int nodeIdx) {
		
	}
	
	public void _enterMethod() {
		
	}
	
	public void _exitMethod() {
		
	}
	
	private boolean doesNotNeedToRecord(String methodId) {
		currentNode.getCorrespondingBranch(methodId);
		return false;
	}
	
	public synchronized static ICoverageTracer _getTracer(String methodId, boolean isEntryPoint) {
		long threadId = Thread.currentThread().getId();
		CoverageTracer tracer = rtStore.get(threadId);
		if (tracer.state != TracingState.RECORDING) {
			if (isEntryPoint) {
				tracer.state = TracingState.RECORDING;
			} else {
				return EmptyCoverageTracer.getInstance();
			}
		}
		if (!isEntryPoint & tracer.doesNotNeedToRecord(methodId)) {
			return EmptyCoverageTracer.getInstance();
		}
		return tracer;
	}

	@Override
	public long getThreadId() {
		return threadId;
	}
}
