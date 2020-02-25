package microbat.instrumentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sav.common.core.utils.CollectionUtils;

/**
 * 
 * @author lyly
 *
 */
public class CommandLine {
	private Map<String, String> argMap = new HashMap<>();
	
	public static CommandLine parse(String agentArgs) {
		CommandLine cmd = new CommandLine();
		String[] args = agentArgs.split(AgentConstants.AGENT_PARAMS_SEPARATOR);
		for (String arg : args) {
			String[] keyValue = arg.split(AgentConstants.AGENT_OPTION_SEPARATOR);
			cmd.argMap.put(keyValue[0], keyValue[1]);
		}
		return cmd;
	}

	public boolean getBoolean(String option, boolean defaultValue) {
		String strVal = getString(option);
		if (strVal != null) {
			return Boolean.valueOf(strVal);
		}
		return defaultValue;
	}

	public int getInt(String option, int defaultValue) {
		String strVal = getString(option);
		if (strVal != null) {
			return Integer.valueOf(strVal);
		}
		return defaultValue;
	}

	public List<String> getStringList(String option) {
		String value = getString(option);
		if (value == null || value.isEmpty()) {
			return new ArrayList<>(0);
		}

		return CollectionUtils.toArrayList(value.split(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR));
	}
	
	public Set<String> getStringSet(String option) {
		String value = getString(option);
		if (value == null || value.isEmpty()) {
			return new HashSet<>(0);
		}

		return CollectionUtils.toHashSet(value.split(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR));
	}

	public String getString(String option) {
		return argMap.get(option);
	}
}
