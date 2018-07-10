package microbat.instrumentation.cfgcoverage;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import microbat.instrumentation.CommandLine;
import microbat.instrumentation.IAgent;
import microbat.instrumentation.cfgcoverage.graph.CoverageGraphConstructor;
import microbat.instrumentation.cfgcoverage.instr.CoverageTransformer;
import microbat.instrumentation.cfgcoverage.instr.InstmInstructions;
import microbat.instrumentation.cfgcoverage.runtime.CoverageTracer;

public class CoverageAgent implements IAgent {
	private CoverageAgentParams agentParams;
	
	public CoverageAgent(CommandLine cmd) {
		this.agentParams = CoverageAgentParams.initFrom(cmd);
	}

	@Override
	public void startup() {
		CoverageGraphConstructor constructor = new CoverageGraphConstructor();
		CoverageTracer.coverageFlowGraph = constructor.buildCoverageGraph(agentParams.initAppClasspath(),
				agentParams.getTargetMethod(), agentParams.getCdgLayer());
		InstmInstructions.initInstrInstructions(CoverageTracer.coverageFlowGraph);
	}

	@Override
	public void shutdown() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClassFileTransformer getTransformer() {
		return new CoverageTransformer(agentParams);
	}

	@Override
	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception {
		// do nothing for now.
	}


}
