package microbat.instrumentation.cfgcoverage;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.CommandLine;
import microbat.instrumentation.IAgent;
import microbat.instrumentation.cfgcoverage.graph.CoverageGraphConstructor;
import microbat.instrumentation.cfgcoverage.graph.CoveragePath;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.instr.CoverageInstrumenter;
import microbat.instrumentation.cfgcoverage.instr.CoverageTransformer;
import microbat.instrumentation.cfgcoverage.instr.MethodInstructionsInfo;
import microbat.instrumentation.cfgcoverage.runtime.CoverageTracer;
import microbat.instrumentation.cfgcoverage.runtime.value.ValueExtractor;
import microbat.instrumentation.filter.FilterChecker;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StopTimer;
import sav.strategies.dto.AppJavaClassPath;

public class CoverageAgent implements IAgent {
	private CoverageAgentParams agentParams;
	private CoverageInstrumenter instrumenter;
	private List<String> testcases = new ArrayList<String>();
	private CoverageTransformer coverageTransformer;
	private StopTimer timer;
	
	public CoverageAgent(CommandLine cmd) {
		this.agentParams = new CoverageAgentParams(cmd);
		coverageTransformer = new CoverageTransformer(agentParams);
		instrumenter = coverageTransformer.getInstrumenter();
	}

	@Override
	public void startup(long vmStartupTime, long agentPreStartup) {
		timer = new AgentStopTimer("Tracing program for coverage", vmStartupTime, agentPreStartup);
		timer.newPoint("Execution");
		AppJavaClassPath appClasspath = agentParams.initAppClasspath();
		FilterChecker.setup(appClasspath, null, null);
		ValueExtractor.variableLayer = agentParams.getVarLayer();
		CoverageGraphConstructor constructor = new CoverageGraphConstructor();
		CoverageSFlowGraph coverageFlowGraph = constructor.buildCoverageGraph(appClasspath,
				agentParams.getTargetMethod(), agentParams.getCdgLayer(), agentParams.getInclusiveMethodIds());
		CoverageTracer.coverageFlowGraph = coverageFlowGraph;
		MethodInstructionsInfo.initInstrInstructions(coverageFlowGraph);
		instrumenter.setEntryPoint(coverageFlowGraph.getStartNode().getStartNodeId().getMethodId());
	}

	@Override
	public void shutdown() throws Exception {
		timer.newPoint("Saving coverage");
		AgentLogger.debug("Saving coverage...");
		CoverageSFlowGraph coverageGraph = CoverageTracer.coverageFlowGraph;
		Map<List<Integer>, List<Integer>> pathMap = new HashMap<>(); // path to tcs
		for (Entry<Integer, List<Integer>> tcPath : CoverageTracer.testcaseGraphExecPaths.entrySet()) {
			CollectionUtils.getListInitIfEmpty(pathMap, tcPath.getValue()).add(tcPath.getKey());
		}
		List<CoveragePath> coveredPaths = new ArrayList<>(pathMap.size());
		for (Entry<List<Integer>, List<Integer>> entry : pathMap.entrySet()) {
			CoveragePath path = new CoveragePath();
			path.setCoveredTcs(entry.getValue());
			List<CoverageSFNode> nodes = new ArrayList<>();
			for (int nodeId : entry.getKey()) {
				nodes.add(coverageGraph.getNodeList().get(nodeId));
			}
			path.setPath(nodes);
			coveredPaths.add(path);
		}
		coverageGraph.setCoveragePaths(coveredPaths);
		CoverageOutput coverageOutput = new CoverageOutput(coverageGraph);
		coverageOutput.setInputData(CoverageTracer.testInputData);
		coverageOutput.saveToFile(agentParams.getDumpFile());
		AgentLogger.debug(timer.getResultString());
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		int testIdx = testcases.size();
		String testcase = InstrumentationUtils.getMethodId(junitClass, junitMethod);
		testcases.add(testcase);
		CoverageTracer.startTestcase(testcase, testIdx);
	}
	
	@Override
	public void exitTest(String testResultMsg, String junitClass, String junitMethod, long threadId) {
		CoverageTracer.endTestcase(InstrumentationUtils.getMethodId(junitClass, junitMethod), threadId);
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		// do nothing for now.
	}

	@Override
	public ClassFileTransformer getTransformer() {
		return coverageTransformer;
	}

	@Override
	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception {
		// do nothing for now.
	}

	@Override
	public boolean isInstrumentationActive() {
		return true;
	}


}
