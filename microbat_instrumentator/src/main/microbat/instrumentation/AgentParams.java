package microbat.instrumentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.trace.InstrConstants;
import microbat.instrumentation.trace.model.EntryPoint;

public class AgentParams {
	public static final String OPT_ENTRY_POINT = "entry_point";
	public static final String OPT_LAUNCH_CLASS = "launch_class";
	public static final String OPT_JAVA_HOME = "java_home";
	public static final String OPT_CLASS_PATH = "class_path";
	public static final String OPT_WORKING_DIR = "working_dir";
	public static final String OPT_TCP_PORT = "tcp_port";
	private EntryPoint entryPoint;
	
	private List<String> classPaths = new ArrayList<>();
	private List<String> bootstrapPaths = new ArrayList<>();
	private String workingDirectory;
	private String javaHome;
	private String launchClass;
	private int tcpPort;

	public static AgentParams parse(String agentArgs) {
		String[] args = agentArgs.split(InstrConstants.AGENT_PARAMS_SEPARATOR);
		Map<String, String> argMap = new HashMap<>();
		for (String arg : args) {
			String[] keyValue = arg.split(InstrConstants.AGENT_OPTION_SEPARATOR);
			argMap.put(keyValue[0], keyValue[1]);
		}
		AgentParams params = new AgentParams();
		
		String entryPointStr = argMap.get(OPT_ENTRY_POINT);
		if (entryPointStr != null) {
			int idx = entryPointStr.lastIndexOf(".");
			String mainClass = entryPointStr.substring(0, idx);
			String mainMethod = entryPointStr.substring(idx + 1);
			
			EntryPoint entryPoint = new EntryPoint(mainClass, mainMethod);
			params.entryPoint = entryPoint;
		}
		
		params.setJavaHome(argMap.get(OPT_JAVA_HOME));
		params.setWorkingDirectory(argMap.get(OPT_WORKING_DIR));

		String launchClass = argMap.get(OPT_LAUNCH_CLASS);
		if (launchClass == null && params.entryPoint != null) {
			launchClass = params.entryPoint.getClassName();
		}
		params.setLaunchClass(launchClass);
		
		String classPathString = argMap.get(OPT_CLASS_PATH);
		String[] classPaths = classPathString.split(";");
		for(String classPath: classPaths){
			params.getClassPaths().add(classPath);
		}
		String portStr = argMap.get(OPT_TCP_PORT);
		if (portStr != null) {
			params.tcpPort = Integer.valueOf(portStr);
		}
//		String bootstrpString = argMap.get("bootstrp_path");
//		String[] bootstrpStrings = bootstrpString.split(";");
//		for(String bootstrp: bootstrpStrings){
//			params.bootstrapPaths.add(bootstrp);
//		}
		
		return params;
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

}
