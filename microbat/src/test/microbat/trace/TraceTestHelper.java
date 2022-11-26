package microbat.trace;

import java.io.File;

import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class TraceTestHelper {
	
	static AppJavaClassPath constructClassPaths(String projectPath){
		AppJavaClassPath appClassPath = new AppJavaClassPath();
		MicroBatUtil.setSystemJars(appClassPath);
		// Get path to target in sample
		String outputPath = projectPath + File.separator + "target"; 
		
		String javaHome = System.getenv("JAVA_8_HOME");
		appClassPath.setJavaHome(javaHome);
		appClassPath.addClasspath(outputPath);
		appClassPath.addClasspath(String.join(File.separator, outputPath, "test-classes"));
		appClassPath.addClasspath(String.join(File.separator, outputPath, "classes"));
		appClassPath.setWorkingDirectory(projectPath);
		appClassPath.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		appClassPath.setOptionalTestClass(Settings.launchClass);
		appClassPath.setOptionalTestMethod(Settings.testMethod);
		appClassPath.setSourceCodePath(String.join(File.separator, projectPath, "src", "main", "java"));
		appClassPath.setTestCodePath(String.join(File.separator, projectPath, "src", "test", "java"));
		return appClassPath;
	}
	
	static void checkEnvVars() {
		String msg = "%s environment variable not yet set. Please specify path to %s in \"Run Configurations -> Environment\".";
		if (System.getenv("ECLIPSE_APP") == null) {
			throw new RuntimeException(String.format(msg, "ECLIPSE_APP", "eclipse.exe"));
		}
		if (System.getenv("JAVA_8_HOME") == null) {
			throw new RuntimeException(String.format(msg, "JAVA_8_HOME", "Java 8"));
		}
		
		String doesNotExistMsg = "Path specified in environment variable %s does not exist.";
		if (!new File(System.getenv("JAVA_8_HOME")).exists()) {
			throw new RuntimeException(String.format(doesNotExistMsg, "JAVA_8_HOME"));
		}
		if (!new File(System.getenv("ECLIPSE_APP")).exists()) {
			throw new RuntimeException(String.format(doesNotExistMsg, "ECLIPSE_APP"));
		}
		System.setProperty("eclipse.launcher", System.getenv("ECLIPSE_APP"));
	}
	
	static void setupSettings() {
		Settings.projectName = "sample0";
		Settings.launchClass = "sample0.junit4.OperationTest";
		Settings.isRunTest = true;
	}
}
