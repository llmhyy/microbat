package microbat.instrumentation.cfgcoverage.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode.Type;
import microbat.instrumentation.cfgcoverage.runtime.value.ValueExtractor;
import microbat.instrumentation.runtime.ITracer;
import microbat.instrumentation.runtime.TracingState;
import microbat.model.BreakPointValue;
import microbat.model.ClassLocation;
import sav.common.core.SavRtException;

public class CoverageTracer implements ICoverageTracer, ITracer {
	public static CoverageTracerStore rtStore = new CoverageTracerStore();
	public static volatile Map<Integer, List<MethodExecutionData>> methodExecsOnASingleTcMap = new HashMap<>();

	protected long threadId;
	private String testcase;
	private int testIdx;
	private TracingState state = TracingState.INIT;
	private ValueExtractor valueExtractor = new ValueExtractor();
	private int methodHierachyLevel = 0;
	private CoverageSFNode currentNode;
	private MethodExecutionData methodExecData;
	
	public CoverageTracer(long threadId, int testIdx) {
		this.threadId = threadId;
		this.testIdx = testIdx;
		this.testcase = AgentRuntimeData.coverageFlowGraph.getCoveredTestcases().get(testIdx);
	}
	
	@Override
	public void _reachNode(String methodId, int nodeIdx) {
		if (state == TracingState.SHUTDOWN) {
			return;
		}
		if (currentNode == null) {
			currentNode = AgentRuntimeData.coverageFlowGraph.getStartNode();
		} else {
			CoverageSFNode branch = currentNode.getCorrespondingBranch(methodId, nodeIdx);
			if (branch != null) {
				currentNode.markCoveredBranch(branch, testcase);
				currentNode = branch;
			} else {
				if (!currentNode.isAliasNode()) {
					AgentLogger.debug(String.format("cannot find branch %s:%d of node %d [testcase=%s]", methodId, nodeIdx,
							currentNode.getCvgIdx(), testcase));
				}
				return;
			}
		}
		methodExecData.appendExecPath(currentNode);
		currentNode.addCoveredTestcase(testcase);
	}
	
	
	@Override
	public void enterMethod(String methodId, String paramTypeSignsCode, String paramNamesCode, Object[] params,
			boolean isEntryPoint, Object receiver) {
		if (isEntryPoint && methodHierachyLevel == 0) {
			/* record a new coverage execution path on target method */
			currentNode = null;
			ClassLocation loc = InstrumentationUtils.getClassLocation(methodId);
			methodExecData = new MethodExecutionData(testIdx);
			List<MethodExecutionData> list = methodExecsOnASingleTcMap.get(testIdx);
			if (list == null) {
				list = new ArrayList<>(1);
				methodExecsOnASingleTcMap.put(testIdx, list);
			}
			list.add(methodExecData);
			BreakPointValue methodInput = valueExtractor.extractInputValue(String.valueOf(testIdx), 
					loc.getClassCanonicalName(), loc.getMethodSign(), paramTypeSignsCode, paramNamesCode, params, receiver);
			methodExecData.setMethodInputValue(methodInput);
		}
		methodHierachyLevel++;
	}
	
	@Override
	public void _exitMethod(String methodId, boolean isEntryPoint) {
		methodHierachyLevel--;
	}
	
	public boolean doesNotNeedToRecord(String methodId) {
		try {
			if (methodHierachyLevel >= AgentRuntimeData.coverageFlowGraph.getExtensionLayer()) {
				return true;
			}
			if (currentNode.getType() != Type.INVOKE_NODE) {
				throw new SavRtException(String.format("Expect INVOKE_NODE node, get %s (%s)", currentNode.getType(), currentNode));
			}
			for (CoverageSFNode branch : currentNode.getBranchTargets()) {
				UniqueNodeId nodeId = branch.getStartNodeId();
				if (nodeId.getMethodId().equals(methodId) && nodeId.getLocalNodeIdx() == 0) {
					return false;
				}
			}
			return true;
		} catch(Throwable t) {
			AgentLogger.error(t);
			return true;
		}
	}
	
	/* collect condition variation value part */
	private double notIntCmpVariation;
	
	@Override
	public void _onDcmp(double value1, double value2) {
		// value2 - value1
		if (value1 < 0) {
			/* check overflow */
			if (Double.MAX_VALUE + value1 < value2) {
				notIntCmpVariation = Double.MAX_VALUE;
			} else {
				notIntCmpVariation = value2 - value1;
			}
		} else {
			if (-Double.MAX_VALUE + value1 > value2) {
				notIntCmpVariation = -Double.MAX_VALUE;
			} else {
				notIntCmpVariation = value2 - value1;
			}
		}
	}
	
	@Override
	public void _onFcmp(float value1, float value2) {
		_onDcmp(value1, value2);
	}
	
	@Override
	public void _onLcmp(long value1, long value2) {
		_onDcmp(value1, value2);
	}
	
	private void onIf(String methodId, int nodeIdx, double condVariation) {
		if (nodeRecording(methodId, nodeIdx)) {
			methodExecData.addConditionVariation(currentNode.getCvgIdx(), condVariation);
		}
	}

	private boolean nodeRecording(String methodId, int nodeLocalIdx) {
		if (currentNode == null) {
			return false;
		}
		return currentNode.getProbeNodeId().match(methodId, nodeLocalIdx);
	}
	
	@Override
	public void _onIfACmp(Object value1, Object value2, String methodId, int nodeIdx) {
		onIf(methodId, nodeIdx, value1 == value2 ? 0 : 1);
	}

	@Override
	public void _onIfICmp(int value1, int value2, String methodId, int nodeIdx) {
		onIf(methodId, nodeIdx, value2 - value1);
	}

	@Override
	public void _onIf(int value, boolean isNotIntCmpIf, String methodId, int nodeIdx) {
		if (isNotIntCmpIf) {
			onIf(methodId, nodeIdx, notIntCmpVariation);
		} else {
			onIf(methodId, nodeIdx, value);
		}
	}

	@Override
	public void _onIfNull(Object value, String methodId, int nodeIdx) {
		onIf(methodId, nodeIdx, value == null ? 0 : 1);
	}
	
	/* end of collect condition variation value part */
	
	public synchronized static ICoverageTracer _getTracer(String methodId, boolean isEntryPoint, String paramNamesCode,
			String paramTypeSignsCode, Object[] params, Object receiver) {
		try {
			long threadId = Thread.currentThread().getId();
			Integer currentTestCaseIdx = AgentRuntimeData.currentTestIdxMap.get(threadId);
			CoverageTracer coverageTracer = rtStore.get(threadId, currentTestCaseIdx);
			if (coverageTracer == null && isEntryPoint) {
				coverageTracer = rtStore.create(threadId, currentTestCaseIdx);
				coverageTracer.setState(TracingState.RECORDING);
				AgentRuntimeData.register(coverageTracer, threadId, currentTestCaseIdx);
			}
			if (coverageTracer == null || coverageTracer.getState() != TracingState.RECORDING) {
				return EmptyCoverageTracer.getInstance();
			}
			ICoverageTracer tracer = coverageTracer;
			if ((!isEntryPoint || coverageTracer.methodHierachyLevel > 0) && coverageTracer.doesNotNeedToRecord(methodId)) {
				tracer = EmptyCoverageTracer.getInstance();
			}
			tracer.enterMethod(methodId, paramTypeSignsCode, paramNamesCode, params, isEntryPoint, receiver);
			return tracer;
		} catch(Throwable t) {
			AgentLogger.error(t);
			return EmptyCoverageTracer.getInstance();
		}
	}

	@Override
	public long getThreadId() {
		return threadId;
	}
	
	public int getTestIdx() {
		return testIdx;
	}
	
	public TracingState getState() {
		return state;
	}

	public void setState(TracingState state) {
		this.state = state;
	}
	
	@Override
	public void shutDown() {
		this.state = TracingState.SHUTDOWN;
	}
}
