package microbat.instrumentation;

import java.util.HashMap;
import java.util.Map;

import microbat.instrumentation.trace.InstrConstants;
import microbat.model.ClassLocation;
import sav.common.core.Pair;
import sav.common.core.utils.ClassUtils;

public class AgentParams {
	private ClassLocation entryPoint;
	
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
		ClassLocation entryPoint = new ClassLocation(classMethod.a, classMethod.b, -1);
		params.entryPoint = entryPoint;
		return params;
	}

	public ClassLocation getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(ClassLocation entryPoint) {
		this.entryPoint = entryPoint;
	}
}
