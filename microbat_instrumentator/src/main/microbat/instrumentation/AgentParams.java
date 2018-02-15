package microbat.instrumentation;

import java.util.HashMap;
import java.util.Map;

import microbat.instrumentation.trace.InstrConstants;
import microbat.instrumentation.trace.model.EntryPoint;
import sav.common.core.Pair;
import sav.common.core.utils.ClassUtils;
import sav.strategies.dto.AppJavaClassPath;

public class AgentParams {
	public static final String OPT_ENTRY_POINT = "entry_point";
	public static final String OPT_LAUNCH_CLASS = "launch_class";
	public static final String OPT_JAVA_HOME = "java_home";
	public static final String OPT_CLASS_PATH = "class_path";
	public static final String OPT_WORKING_DIR = "working_dir";
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
		String entryPointStr = argMap.get(OPT_ENTRY_POINT);
		if (entryPointStr != null) {
			Pair<String, String> classMethod = ClassUtils.splitClassMethod(entryPointStr);
			EntryPoint entryPoint = new EntryPoint(classMethod.a, classMethod.b);
			params.entryPoint = entryPoint;
		}
		String launchClass = argMap.get(OPT_LAUNCH_CLASS);
		if (launchClass == null && params.entryPoint != null) {
			launchClass = params.entryPoint.getClassName();
		}
		String javaHome = argMap.get(OPT_JAVA_HOME);
		String classPathString = argMap.get(OPT_CLASS_PATH);
		String[] classPaths = classPathString.split(";");
		String workingDirectory = argMap.get(OPT_WORKING_DIR);
		
		AppJavaClassPath appPath = new AppJavaClassPath();
		appPath.setJavaHome(javaHome);
		appPath.setWorkingDirectory(workingDirectory);
		appPath.setLaunchClass(launchClass);
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
