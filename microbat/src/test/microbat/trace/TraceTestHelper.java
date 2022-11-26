package microbat.trace;

import java.io.File;
import java.util.ArrayList;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class TraceTestHelper {
	// Please modify these fields for running JUnit plug-in tests for microbat
	private static final String JAVA_8_HOME = "C:\\Tools\\Java\\jdk1.8.0_202";
	private static final String ECLIPSE_EXE = "C:\\Users\\bchenghi\\eclipse\\java-2022-09\\eclipse\\eclipse.exe";
	
	/**
	 * Creates AppJavaClassPath for creating trace
	 * @param projectPath
	 * @return
	 */
	static AppJavaClassPath constructClassPaths(String projectPath){
		AppJavaClassPath appClassPath = new AppJavaClassPath();
		MicroBatUtil.setSystemJars(appClassPath);
		// Get path to target in sample
		String outputPath = projectPath + File.separator + "target"; 
		
		appClassPath.setJavaHome(JAVA_8_HOME);
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
	
	/**
	 * Check if fields are set up
	 */
	static void checkEnvVars() {
		String doesNotExistMsg = "Path specified in TraceTestHelper for %s does not exist.";
		if (!new File(JAVA_8_HOME).exists()) {
			throw new RuntimeException(String.format(doesNotExistMsg, "JAVA_8_HOME"));
		}
		if (!new File(ECLIPSE_EXE).exists()) {
			throw new RuntimeException(String.format(doesNotExistMsg, "ECLIPSE_APP"));
		}
	}
	
	/**
	 * Set up global settings
	 * @param projectName
	 * @param className
	 * @param methodName
	 */
	static void setupSettings(String projectName, String className, String methodName) {		
		Settings.launchClass = className;
		Settings.testMethod = methodName;
		Settings.projectName = projectName;
		Settings.isRunTest = true;
		System.setProperty("eclipse.launcher", ECLIPSE_EXE);
	}
	
	/**
	 * Creates a trace given the test class, method and project name
	 * @param projectName
	 * @param className
	 * @param methodName
	 * @return
	 * @throws StepLimitException
	 */
	static Trace createTrace(String projectName, String className, String methodName) throws StepLimitException {
		setupSettings(projectName, className, methodName);
		AppJavaClassPath appClassPath = constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		return result.getMainTrace();
	}
}
