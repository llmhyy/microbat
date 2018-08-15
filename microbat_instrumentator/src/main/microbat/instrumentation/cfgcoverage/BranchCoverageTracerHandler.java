package microbat.instrumentation.cfgcoverage;

import microbat.instrumentation.cfgcoverage.CoverageAgent.ICoverageTracerHandler;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.runtime.AgentRuntimeData;

public class BranchCoverageTracerHandler implements ICoverageTracerHandler {

	@Override
	public CoverageOutput getCoverageOutput() {
		CoverageSFlowGraph coverageGraph = AgentRuntimeData.coverageFlowGraph;
		CoverageOutput coverageOutput = new CoverageOutput(coverageGraph);
		return coverageOutput;
	}

	@Override
	public void reset() {
		AgentRuntimeData.coverageFlowGraph.clearData();
		AgentRuntimeData.currentTestIdxMap.clear();
	}

}
