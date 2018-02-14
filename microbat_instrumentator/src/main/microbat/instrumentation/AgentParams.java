package microbat.instrumentation;

import java.util.HashMap;
import java.util.Map;

import microbat.instrumentation.trace.InstrConstants;
import microbat.instrumentation.trace.model.EntryPoint;
import sav.common.core.Pair;
import sav.common.core.utils.ClassUtils;
import sav.strategies.dto.AppJavaClassPath;

public class AgentParams {
	private EntryPoint entryPoint;
	private AppJavaClassPath appPath;

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
		
		
		String javaHome = argMap.get("java_home");
		String classPathString = argMap.get("class_path");
		String[] classPaths = classPathString.split(";");
		String workingDirectory = argMap.get("working_dir");
		
		AppJavaClassPath appPath = new AppJavaClassPath();
		appPath.setJavaHome(javaHome);
		appPath.setWorkingDirectory(workingDirectory);
		appPath.setLaunchClass(classMethod.a);
		for(String classPath: classPaths){
			appPath.addClasspath(classPath);
		}
		params.setAppPath(appPath);
		
		return params;
	}

	public EntryPoint getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	public AppJavaClassPath getAppPath() {
		return appPath;
	}

	public void setAppPath(AppJavaClassPath appPath) {
		this.appPath = appPath;
	}

}
