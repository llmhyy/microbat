package microbat.instrumentation.cfgcoverage.runtime;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.runtime.ITracer;
import microbat.instrumentation.runtime.TracingState;

public class CoverageTracer implements ICoverageTracer, ITracer {
	private static CoverageTracerStore rtStore = new CoverageTracerStore();
	public static CoverageSFlowGraph coverageFlowGraph;
	private static int currentTestCaseIdx;
	
	private long threadId;
	private TracingState state = TracingState.INIT;
	private NoProbeTracer noProbeTracer = new NoProbeTracer(this);
	private int methodInvokeLevel = 0;
	private int testIdx;
	private CoverageSFNode currentNode;
	MethodCallStack methodCallStack = new MethodCallStack();
	
	public CoverageTracer(long threadId) {
		this.threadId = threadId;
	}
	
	@Override
	public void _reachNode(String methodId, int nodeIdx) {
		if (currentNode == null) {
			currentNode = coverageFlowGraph.getStartNode();
			testIdx = currentTestCaseIdx;
		} else {
			CoverageSFNode branch = currentNode.getCorrespondingBranch(methodId, nodeIdx);
			// currentNode should not be null here.
			branch.addCoveredTestcase(testIdx);
			currentNode.markCoveredBranch(branch, testIdx);
			currentNode = branch;
		}
		if (currentNode.getAliasId() != null) {
			currentNode = coverageFlowGraph.getCoverageNode(currentNode.getAliasId().getOrgNodeIdx());
		}
	}
	
	@Override
	public void _enterMethod(String methodId, String paramTypeSignsCode, Object[] params) {
		// TODO-LLT: extract input variable values for learning to replace TestcaseExecutor component.
		methodInvokeLevel++;
		methodCallStack.push(methodId);
	}
	
	@Override
	public void _exitMethod(String methodId) {
		methodInvokeLevel--;
		methodCallStack.safePop();
	}
	
	private boolean doesNotNeedToRecord(String methodId) {
		if (methodInvokeLevel >= coverageFlowGraph.getCdgLayer() 
				|| methodCallStack.size() > methodInvokeLevel) {
			return true;
		}
		CoverageSFNode correspondingNode = currentNode.getCorrespondingBranch(methodId);
		if (correspondingNode == null) {
			return true;
		}
		return false;
	}
	
	public synchronized static ICoverageTracer _getTracer(String methodId, boolean isEntryPoint, String paramNamesCode,
			String paramTypeSignsCode, Object[] params) {
		long threadId = Thread.currentThread().getId();
		CoverageTracer coverageTracer = rtStore.get(threadId);
		if (coverageTracer.state != TracingState.RECORDING) {
			if (isEntryPoint) {
				coverageTracer.state = TracingState.RECORDING;
				rtStore.setMainThreadId(Thread.currentThread().getId());
			} else {
				return EmptyCoverageTracer.getInstance();
			}
		}
		ICoverageTracer tracer = coverageTracer;
		if (!isEntryPoint & coverageTracer.doesNotNeedToRecord(methodId)) {
			tracer = coverageTracer.noProbeTracer;
		}
		tracer._enterMethod(methodId, paramTypeSignsCode, params);
		return tracer;
	}

	@Override
	public long getThreadId() {
		return threadId;
	}
	
	public static void startTestcase(String testcase, int testcaseIdx) {
		coverageFlowGraph.addCoveredTestcase(testcase, testcaseIdx);
		CoverageTracer.currentTestCaseIdx = testcaseIdx;
	}
	
	public static void endTestcase(String testcase) {
		long threadId = Thread.currentThread().getId();
		CoverageTracer coverageTracer = rtStore.get(threadId);
		coverageTracer.state = TracingState.SHUTDOWN;
	}
	
	public static CoverageTracer getMainThreadStore() {
		return rtStore.getMainThreadTracer();
	}
}
