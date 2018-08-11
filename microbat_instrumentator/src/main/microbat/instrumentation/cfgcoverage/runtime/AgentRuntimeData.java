package microbat.instrumentation.cfgcoverage.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;

public class AgentRuntimeData {
	public static volatile CoverageSFlowGraph coverageFlowGraph;
	public static volatile Map<Integer, List<MethodExecutionData>> methodExecsOnASingleTcMap = new HashMap<>();
	public static volatile Map<Long, Integer> currentTestIdxMap = new HashMap<>();
	
}
