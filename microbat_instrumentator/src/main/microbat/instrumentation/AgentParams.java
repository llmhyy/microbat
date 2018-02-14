package microbat.instrumentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.trace.InstrConstants;
import microbat.instrumentation.trace.model.EntryPoint;

public class AgentParams {
	private EntryPoint entryPoint;
	
	private List<String> classPaths = new ArrayList<>();
	private List<String> bootstrapPaths = new ArrayList<>();
	private String workingDirectory;
	private String javaHome;
	private String launchClass;

	public static AgentParams parse(String agentArgs) {
		String[] args = agentArgs.split(InstrConstants.AGENT_PARAMS_SEPARATOR);
		Map<String, String> argMap = new HashMap<>();
		for (String arg : args) {
			String[] keyValue = arg.split(InstrConstants.AGENT_OPTION_SEPARATOR);
			argMap.put(keyValue[0], keyValue[1]);
		}
		AgentParams params = new AgentParams();
		String entryPointStr = argMap.get("entry_point");
		int idx = entryPointStr.lastIndexOf(".");
		String mainClass = entryPointStr.substring(0, idx);
		String mainMethod = entryPointStr.substring(idx + 1);
		
		EntryPoint entryPoint = new EntryPoint(mainClass, mainMethod);
		params.entryPoint = entryPoint;
		
		
		params.setJavaHome(argMap.get("java_home"));
		params.setWorkingDirectory(argMap.get("working_dir"));
		
		params.setLaunchClass(mainClass);
		String classPathString = argMap.get("class_path");
		String[] classPaths = classPathString.split(";");
		for(String classPath: classPaths){
			params.getClassPaths().add(classPath);
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

}
