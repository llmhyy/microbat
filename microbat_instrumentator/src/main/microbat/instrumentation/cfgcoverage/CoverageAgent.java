package microbat.instrumentation.cfgcoverage;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.CommandLine;
import microbat.instrumentation.IAgent;
import microbat.instrumentation.cfgcoverage.CoverageAgentParams.CoverageCollectionType;
import microbat.instrumentation.cfgcoverage.graph.CoverageGraphConstructor;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.instr.CoverageInstrumenter;
import microbat.instrumentation.cfgcoverage.instr.CoverageTransformer;
import microbat.instrumentation.cfgcoverage.instr.MethodInstructionsInfo;
import microbat.instrumentation.cfgcoverage.output.CoverageOutputWriter;
import microbat.instrumentation.cfgcoverage.runtime.AgentRuntimeData;
import microbat.instrumentation.cfgcoverage.runtime.value.ValueExtractor;
import microbat.instrumentation.filter.GlobalFilterChecker;
import sav.common.core.utils.StopTimer;
import sav.strategies.dto.AppJavaClassPath;

public class CoverageAgent implements IAgent {
	private CoverageAgentParams agentParams;
	private CoverageInstrumenter instrumenter;
	private CoverageTransformer coverageTransformer;
	private StopTimer timer;
	private ICoverageTracerHandler tracerHandler;
	
	public CoverageAgent(CommandLine cmd) {
		this.agentParams = new CoverageAgentParams(cmd);
		coverageTransformer = new CoverageTransformer(agentParams);
		instrumenter = coverageTransformer.getInstrumenter();
		switch (agentParams.getCoverageType()) {
		case BRANCH_COVERAGE:
			tracerHandler = new BranchCoverageTracerHandler();
			break;
		case UNCIRCLE_CFG_COVERAGE:
			tracerHandler = new CFGCoverageHandler();
			break;
		}
	}

	@Override
	public void startup(long vmStartupTime, long agentPreStartup) {
		timer = new AgentStopTimer("Tracing program for coverage", vmStartupTime, agentPreStartup);
		timer.newPoint("initGraph");
		AppJavaClassPath appClasspath = agentParams.initAppClasspath();
		GlobalFilterChecker.setup(appClasspath, null, null);
		ValueExtractor.variableLayer = agentParams.getVarLayer();
		CoverageGraphConstructor constructor = new CoverageGraphConstructor();
		CoverageSFlowGraph coverageFlowGraph = constructor.buildCoverageGraph(appClasspath,
				agentParams.getTargetMethod(), agentParams.getCdgLayer(), agentParams.getInclusiveMethodIds(),
				agentParams.getCoverageType() == CoverageCollectionType.UNCIRCLE_CFG_COVERAGE);
		AgentRuntimeData.coverageFlowGraph = coverageFlowGraph;
		MethodInstructionsInfo.initInstrInstructions(coverageFlowGraph);
		instrumenter.setEntryPoint(coverageFlowGraph.getStartNode().getStartNodeId().getMethodId());
		timer.newPoint("Execution");
	}

	@Override
	public void shutdown() throws Exception {
		if (agentParams.getDumpFile() != null) {
			timer.newPoint("Saving coverage");
			AgentLogger.debug("Saving coverage...");
			CoverageOutput coverageOutput = tracerHandler.getCoverageOutput();
			coverageOutput.saveToFile(agentParams.getDumpFile());
			AgentLogger.debug(timer.getResultString());
		}
	}
	
	public static void _storeCoverage(OutputStream outStream, Boolean reset) {
		try {
			AgentLogger.debug("Saving coverage...");
			CoverageAgent coverageAgent = (CoverageAgent) Agent.getAgent();
			CoverageOutput coverageOutput = coverageAgent.tracerHandler.getCoverageOutput();
			@SuppressWarnings("resource")
			CoverageOutputWriter coverageOutputWriter = new CoverageOutputWriter(outStream);
			synchronized (coverageOutput.getCoverageGraph()) {
				coverageOutputWriter.writeCfgCoverage(coverageOutput.getCoverageGraph());
				coverageOutputWriter.writeInputData(coverageOutput.getInputData());
				coverageOutputWriter.flush();
				if (reset) {
					coverageAgent.tracerHandler.reset();
				}
			}
		} catch (IOException e) {
			AgentLogger.error(e);
			e.printStackTrace();
		} 
		AgentLogger.debug("Finish saving coverage...");
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		String testcase = InstrumentationUtils.getMethodId(junitClass, junitMethod);
		int testIdx = AgentRuntimeData.coverageFlowGraph.addCoveredTestcase(testcase);
		AgentRuntimeData.currentTestIdxMap.put(Thread.currentThread().getId(), testIdx);
		AgentLogger.debug(String.format("Start testcase %s, testIdx=%s", testcase, testIdx));
	}
	
	@Override
	public void exitTest(String testResultMsg, String junitClass, String junitMethod, long threadId) {
		Integer testIdx = AgentRuntimeData.currentTestIdxMap.get(threadId);
		AgentLogger.debug(String.format("Exit testcase %s, testIdx=%s, thread=%s",
				InstrumentationUtils.getMethodId(junitClass, junitMethod),
				testIdx, threadId));
		if (testIdx != null) {
			AgentRuntimeData.unregister(threadId, testIdx);
		}
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

	public static interface ICoverageTracerHandler {

		CoverageOutput getCoverageOutput();

		void reset();
		
	}
}
