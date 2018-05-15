package microbat.instrumentation;

import java.io.File;

public class AgentConstants {
	private AgentConstants() {}
	
	public static final int UNKNOWN_LINE = -1;
	public static final String AGENT_OPTION_SEPARATOR = "=";
	public static final String AGENT_PARAMS_SEPARATOR = ",";
	public static final String AGENT_PARAMS_MULTI_VALUE_SEPARATOR = File.pathSeparator;
	public static final String PROGRESS_HEADER = "$progress ";
	public static final String LOG_HEADER = "Agent: ";
	public static final int UNSPECIFIED_INT_VALUE = -1; 
}
