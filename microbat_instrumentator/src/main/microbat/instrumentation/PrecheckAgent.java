package microbat.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.List;

import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.instrumentation.precheck.PrecheckTransformer;
import microbat.instrumentation.precheck.TraceMeasurement;

public class PrecheckAgent implements IAgent {
	private AgentParams agentParams;
	private PrecheckTransformer precheckTransformer;

	public PrecheckAgent(CommandLine cmd) {
		this.agentParams = AgentParams.initFrom(cmd); 
		this.precheckTransformer = new PrecheckTransformer(agentParams);
	}
	
	public void startup(long vmStartupTime, long agentPreStartup) {
		FilterChecker.setup(agentParams.initAppClassPath(), agentParams.getIncludesExpression(),
				agentParams.getExcludesExpression());
		TraceMeasurement.setStepLimit(agentParams.getStepLimit());
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

	public PrecheckTransformer getTransformer() {
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
	public boolean isInstrumentationActive() {
		return true;
	}
}
