package microbat.instrumentation.trace;

import sav.strategies.vm.AgentVmRunner;

public class TraceAgentVmRunner extends AgentVmRunner {

	public TraceAgentVmRunner(String agentJarPath) {
		super(agentJarPath, InstrConstants.AGENT_OPTION_SEPARATOR, InstrConstants.AGENT_PARAMS_SEPARATOR);
	}

}
