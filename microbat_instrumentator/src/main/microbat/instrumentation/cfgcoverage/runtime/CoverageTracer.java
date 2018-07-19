package microbat.instrumentation.cfgcoverage.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.runtime.ITracer;
import microbat.instrumentation.runtime.TracerStore;
import microbat.instrumentation.runtime.TracingState;

public class CoverageTracer implements ICoverageTracer, ITracer {
	private static CoverageTracerStore rtStore = new CoverageTracerStore();
	public static volatile CoverageSFlowGraph coverageFlowGraph;
	public static volatile Map<Integer, List<Integer>> testcaseGraphExecPaths = new HashMap<>();
	private static int currentTestCaseIdx;
	
	private long threadId;
	private TracingState state = TracingState.INIT;
	private NoProbeTracer noProbeTracer = new NoProbeTracer(this);
	private int methodInvokeLevel = 0;
	private int testIdx;
	private CoverageSFNode currentNode;
	private List<Integer> execPath;
	MethodCallStack methodCallStack = new MethodCallStack();
	public CoverageTracer(long threadId) {
		this.threadId = threadId;
	}
	
	@Override
	public void _reachNode(String methodId, int nodeIdx) {
		if (currentNode == null) {
			currentNode = coverageFlowGraph.getStartNode();
			testIdx = currentTestCaseIdx;
			execPath = new ArrayList<>();
			testcaseGraphExecPaths.put(testIdx, execPath);
		} else {
			CoverageSFNode branch = currentNode.getCorrespondingBranch(methodId, nodeIdx);
			if (branch == null && !currentNode.isAliasNode()) {
				AgentLogger.debug(String.format("cannnot find branch %s:%d of node %d", methodId, nodeIdx,
						currentNode.getEndIdx()));
				return;
			}
			// currentNode should not be null here.
			currentNode.markCoveredBranch(branch, testIdx);
			currentNode = branch;
		}
		execPath.add(currentNode.getCvgIdx());
		currentNode.addCoveredTestcase(testIdx);
	}
	
	@Override
	public void enterMethod(String methodId, String paramTypeSignsCode, Object[] params) {
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
		try {
			if (methodInvokeLevel >= coverageFlowGraph.getCdgLayer() 
					|| methodCallStack.size() > methodInvokeLevel) {
				return true;
			}
			CoverageSFNode correspondingNode = currentNode.getCorrespondingBranch(methodId);
			if (correspondingNode == null) {
				return true;
			}
			return false;
		} catch(Throwable t) {
			AgentLogger.error(t);
			return false;
		}
	}
	
	public synchronized static ICoverageTracer _getTracer(String methodId, boolean isEntryPoint, String paramNamesCode,
			String paramTypeSignsCode, Object[] params) {
		long threadId = Thread.currentThread().getId();
		CoverageTracer coverageTracer = rtStore.get(threadId);
		if ((rtStore.getMainThreadId() == TracerStore.INVALID_THREAD_ID) || (coverageTracer.state != TracingState.RECORDING)) {
			if (isEntryPoint) {
				rtStore.setMainThreadId(Thread.currentThread().getId());
				coverageTracer = rtStore.get(threadId);
				coverageTracer.state = TracingState.RECORDING;
			} else {
				return EmptyCoverageTracer.getInstance();
			}
		}
		ICoverageTracer tracer = coverageTracer;
		if (!isEntryPoint && coverageTracer.doesNotNeedToRecord(methodId)) {
			tracer = coverageTracer.noProbeTracer;
		}
		tracer.enterMethod(methodId, paramTypeSignsCode, params);
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
		coverageTracer.currentNode = null;
		coverageTracer.methodInvokeLevel = 0;
		coverageTracer.methodCallStack.clear();
		coverageTracer.execPath = null;
	}
	
	public static CoverageTracer getMainThreadStore() {
		return rtStore.getMainThreadTracer();
	}
}
