package microbat.instrumentation;

import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.instrumentation.precheck.PrecheckTransformer;
import microbat.instrumentation.precheck.TraceMeasurement;
import sav.common.core.utils.TextFormatUtils;

public class PrecheckAgent {
	private AgentParams agentParams;
	private PrecheckTransformer precheckTransformer;

	public PrecheckAgent(AgentParams agentParams, PrecheckTransformer precheckTransformer) {
		this.agentParams = agentParams; 
		this.precheckTransformer = precheckTransformer;
	}
	
	public void startup() {
		FilterChecker.setup(agentParams.initAppClassPath(), agentParams.getIncludesExpression(),
				agentParams.getExcludesExpression());
		TraceMeasurement.setStepLimit(agentParams.getStepLimit());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					shutdown();	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected void shutdown() {
		PrecheckInfo precheckInfo = TraceMeasurement.getPrecheckInfo();
		precheckInfo.setExceedingLimitMethods(precheckTransformer.getExceedingLimitMethods());
		precheckInfo.setProgramMsg(Agent.getProgramMsg());
		System.out.println(TextFormatUtils.printCol(precheckInfo.getExceedingLimitMethods()));
		if (agentParams.getDumpFile() != null) {
			precheckInfo.saveToFile(agentParams.getDumpFile(), false);
		}
	}

	

}
