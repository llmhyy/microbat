package microbat.instrumentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.instr.instruction.info.EntryPoint;
import sav.common.core.utils.CollectionUtils;
import sav.strategies.dto.AppJavaClassPath;

public class AgentParams {
	public static final String OPT_PRECHECK = "precheck";
	public static final String OPT_ENTRY_POINT = "entry_point";
	public static final String OPT_LAUNCH_CLASS = "launch_class";
	public static final String OPT_JAVA_HOME = "java_home";
	public static final String OPT_CLASS_PATH = "class_path";
	public static final String OPT_WORKING_DIR = "working_dir";
	public static final String OPT_TCP_PORT = "tcp_port";
	public static final String OPT_DUMP_FILE = "dump_file_path";
	public static final String OPT_INCLUDES = "includes";
	public static final String OPT_EXCLUDES = "excludes";
	public static final String OPT_VARIABLE_LAYER = "varLayer";
	public static final String OPT_STEP_LIMIT = "stepLimit";
	
	private boolean precheck;
	private EntryPoint entryPoint;
	
	private List<String> classPaths = new ArrayList<>();
	private List<String> bootstrapPaths = new ArrayList<>();
	private String workingDirectory;
	private String javaHome;
	private String launchClass;
	private int tcpPort = -1;
	private String dumpFile;
	private int variableLayer;
	/* format: java.lang.*;java.util.ArrayList;java.util.*\;java.util.Arrays*        */
	private String includesExpression;
	private String excludesExpression;
	private int stepLimit;
	
	public static AgentParams parse(String agentArgs) {
		CommandLine cmd = CommandLine.parse(agentArgs);
		AgentParams params = new AgentParams();
		
		params.precheck = cmd.getBoolean(OPT_PRECHECK, false);
		String entryPointStr = cmd.getString(OPT_ENTRY_POINT);
		if (entryPointStr != null) {
			int idx = entryPointStr.lastIndexOf(".");
			String mainClass = entryPointStr.substring(0, idx);
			String mainMethod = entryPointStr.substring(idx + 1);
			
			EntryPoint entryPoint = new EntryPoint(mainClass, mainMethod);
			params.entryPoint = entryPoint;
		}
		
		params.setJavaHome(cmd.getString(OPT_JAVA_HOME));
		params.setWorkingDirectory(cmd.getString(OPT_WORKING_DIR));

		String launchClass = cmd.getString(OPT_LAUNCH_CLASS);
		if (launchClass == null && params.entryPoint != null) {
			launchClass = params.entryPoint.getClassName();
		}
		params.setLaunchClass(launchClass);
		
		params.classPaths = cmd.getStrings(OPT_CLASS_PATH);
		params.tcpPort = cmd.getInt(OPT_TCP_PORT, -1);
		params.dumpFile = cmd.getString(OPT_DUMP_FILE);
		params.includesExpression = cmd.getString(OPT_INCLUDES);
		params.excludesExpression = cmd.getString(OPT_EXCLUDES);
		params.variableLayer = cmd.getInt(OPT_VARIABLE_LAYER, 2);
		//		String bootstrpString = argMap.get("bootstrp_path");
//		String[] bootstrpStrings = bootstrpString.split(";");
//		for(String bootstrp: bootstrpStrings){
//			params.bootstrapPaths.add(bootstrp);
//		}
		params.stepLimit = cmd.getInt(OPT_STEP_LIMIT, Integer.MAX_VALUE);
		return params;
	}
	
	private static class CommandLine {
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

		public List<String> getStrings(String option) {
			String value = getString(option);
			if (value == null || value.isEmpty()) {
				return new ArrayList<>(0);
			}

			return CollectionUtils.toArrayList(value.split(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR));
		}

		public String getString(String option) {
			return argMap.get(option);
		}
	}

	public EntryPoint getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	public List<String> getBootstrpPaths() {
		return bootstrapPaths;
	}

	public void setBootstrpPaths(List<String> bootstrpPaths) {
		this.bootstrapPaths = bootstrpPaths;
	}

	public List<String> getClassPaths() {
		return classPaths;
	}

	public void setClassPaths(List<String> classPaths) {
		this.classPaths = classPaths;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getJavaHome() {
		return javaHome;
	}

	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}

	public String getLaunchClass() {
		return launchClass;
	}

	public void setLaunchClass(String launchClass) {
		this.launchClass = launchClass;
	}
	
	public int getTcpPort() {
		return tcpPort;
	}

	public String getDumpFile() {
		return dumpFile;
	}

	public String getExcludesExpression() {
		return excludesExpression;
	}
	
	public String getIncludesExpression() {
		return includesExpression;
	}
	
	public int getVariableLayer() {
		return variableLayer;
	}
	
	public boolean isPrecheck() {
		return precheck;
	}
	
	public int getStepLimit() {
		return stepLimit;
	}

	public AppJavaClassPath initAppClassPath() {
		AppJavaClassPath appPath = new AppJavaClassPath();
		appPath.setLaunchClass(getLaunchClass());
		appPath.setJavaHome(getJavaHome());
		for(String cp: getClassPaths()){
			appPath.addClasspath(cp);
		}
		appPath.setWorkingDirectory(getWorkingDirectory());
		return appPath;
	}
}
