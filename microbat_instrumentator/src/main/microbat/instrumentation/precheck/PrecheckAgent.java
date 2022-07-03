package microbat.instrumentation.precheck;

import java.lang.instrument.Instrumentation;
import java.util.List;

import microbat.instrumentation.Agent;
import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.CommandLine;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.instrumentation.instr.SystemClassTransformer;

public class PrecheckAgent extends Agent{
	private AgentParams agentParams;
	private PrecheckTransformer precheckTransformer;
	private Instrumentation instrumentation;

	public PrecheckAgent(CommandLine cmd, Instrumentation instrumentation) {
		this.agentParams = AgentParams.initFrom(cmd); 
		this.precheckTransformer = new PrecheckTransformer(agentParams);
		this.instrumentation = instrumentation;
	}
	
	public void startup0(long vmStartupTime, long agentPreStartup) {
		GlobalFilterChecker.setup(agentParams.initAppClassPath(), agentParams.getIncludesExpression(),
				agentParams.getExcludesExpression());
		TraceMeasurement.setStepLimit(agentParams.getStepLimit());
		SystemClassTransformer.transformThread(instrumentation);
	}

	public void shutdown() {
		PrecheckInfo precheckInfo = TraceMeasurement.getPrecheckInfo();
		precheckInfo.setExceedingLimitMethods(precheckTransformer.getExceedingLimitMethods());
		precheckInfo.setProgramMsg(Agent.getProgramMsg());
		precheckInfo.setLoadedClasses(precheckTransformer.getLoadedClasses());
//		precheckInfo.setThreadNum(Agent.getNumberOfThread());
		AgentLogger.debug(precheckInfo.toString());
		if (agentParams.getDumpFile() != null) {
			precheckInfo.saveToFile(agentParams.getDumpFile(), false);
		}
	}

	public PrecheckTransformer getTransformer0() {
		return precheckTransformer;
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		TraceMeasurement._start();
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		TraceMeasurement.shutdown();
	}

	@Override
	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception {
		List<String> loadedClasses = precheckTransformer.getLoadedClasses();
		for (Class<?> clazz : retransformableClasses) {
			loadedClasses.add(clazz.getName());
		}
	}

	@Override
	public void exitTest(String testResultMsg, String junitClass, String junitMethod, long threadId) {
		// do nothing, not used.
	}

	@Override
	public boolean isInstrumentationActive0() {
		return true;
	}
}
