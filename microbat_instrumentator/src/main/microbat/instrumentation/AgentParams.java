package microbat.instrumentation;

import java.util.HashMap;
import java.util.Map;

import microbat.instrumentation.trace.InstrConstants;
import microbat.instrumentation.trace.model.EntryPoint;
import sav.common.core.Pair;
import sav.common.core.utils.ClassUtils;

public class AgentParams {
	private EntryPoint entryPoint;

	public static AgentParams parse(String agentArgs) {
		String[] args = agentArgs.split(InstrConstants.AGENT_PARAMS_SEPARATOR);
		Map<String, String> argMap = new HashMap<>();
		for (String arg : args) {
			String[] keyValue = arg.split(InstrConstants.AGENT_OPTION_SEPARATOR);
			argMap.put(keyValue[0], keyValue[1]);
		}
		AgentParams params = new AgentParams();
		String entryPointStr = argMap.get("entry_point");
		Pair<String, String> classMethod = ClassUtils.splitClassMethod(entryPointStr);
		EntryPoint entryPoint = new EntryPoint(classMethod.a, classMethod.b);
		params.entryPoint = entryPoint;
		return params;
	}

	public EntryPoint getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

}
