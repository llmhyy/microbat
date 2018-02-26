package microbat.instrumentation;

import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.instrumentation.precheck.TraceMeasurement;

public class PrecheckAgent {
	private AgentParams agentParams;

	public PrecheckAgent(AgentParams agentParams) {
		this.agentParams = agentParams; 
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
		if (agentParams.getDumpFile() != null) {
			precheckInfo.saveToFile(agentParams.getDumpFile(), false);
		}
	}

	

}
