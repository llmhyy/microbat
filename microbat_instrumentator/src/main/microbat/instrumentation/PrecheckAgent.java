package microbat.instrumentation;

import java.util.List;

import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.instrumentation.precheck.PrecheckTransformer;
import microbat.instrumentation.precheck.TraceMeasurement;

public class PrecheckAgent implements IAgent {
	private AgentParams agentParams;
	private PrecheckTransformer precheckTransformer;

	public PrecheckAgent(AgentParams agentParams) {
		this.agentParams = agentParams; 
		this.precheckTransformer = new PrecheckTransformer(agentParams);
	}
	
	public void startup() {
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
	public void setTransformableClasses(Class<?>[] retransformableClasses) {
		List<String> loadedClasses = precheckTransformer.getLoadedClasses();
		for (Class<?> clazz : retransformableClasses) {
			loadedClasses.add(clazz.getName());
		}
	}
}
