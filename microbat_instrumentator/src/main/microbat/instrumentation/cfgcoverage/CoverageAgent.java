package microbat.instrumentation.cfgcoverage;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.CommandLine;
import microbat.instrumentation.IAgent;
import microbat.instrumentation.cfgcoverage.graph.CoverageGraphConstructor;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.instr.CoverageInstrumenter;
import microbat.instrumentation.cfgcoverage.instr.CoverageTransformer;
import microbat.instrumentation.cfgcoverage.instr.MethodInstructionsInfo;
import microbat.instrumentation.cfgcoverage.runtime.CoverageTracer;
import sav.common.core.utils.ClassUtils;

public class CoverageAgent implements IAgent {
	private CoverageAgentParams agentParams;
	private CoverageInstrumenter instrumenter;
	private List<String> testcases = new ArrayList<String>();
	
	public CoverageAgent(CommandLine cmd) {
		this.agentParams = CoverageAgentParams.initFrom(cmd);
	}

	@Override
	public void startup() {
		CoverageGraphConstructor constructor = new CoverageGraphConstructor();
		CoverageSFlowGraph coverageFlowGraph = constructor.buildCoverageGraph(agentParams.initAppClasspath(),
				agentParams.getTargetMethod(), agentParams.getCdgLayer());
		CoverageTracer.coverageFlowGraph = coverageFlowGraph;
		MethodInstructionsInfo.initInstrInstructions(coverageFlowGraph);
		instrumenter.setEntryPoint(coverageFlowGraph.getStartNode().getStartNodeId().getMethodId());
	}

	@Override
	public void shutdown() throws Exception {
		CoverageSFlowGraph coverageGraph = CoverageTracer.coverageFlowGraph;
		CoverageOutput coverageOutput = new CoverageOutput(coverageGraph);
		coverageOutput.saveToFile(agentParams.getDumpFile());
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		int testIdx = testcases.size();
		String testcase = ClassUtils.toClassMethodStr(junitClass, junitMethod);
		testcases.add(testcase);
		CoverageTracer.startTestcase(testcase, testIdx);
	}
	
	@Override
	public void exitTest(String testResultMsg, String junitClass, String junitMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		// do nothing for now.
	}

	@Override
	public ClassFileTransformer getTransformer() {
		CoverageTransformer coverageTransformer = new CoverageTransformer(agentParams);
		instrumenter = coverageTransformer.getInstrumenter();
		return coverageTransformer;
	}

	@Override
	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception {
		// do nothing for now.
	}


}
