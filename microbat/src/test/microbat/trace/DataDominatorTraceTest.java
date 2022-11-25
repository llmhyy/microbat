package microbat.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.VarValue;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class DataControlTraceTest {
	@BeforeClass
	public static void beforeClassSetUp() {
		checkEnvVars();
		setupSettings();
	}
	
	@Test
	public void testAssigment() throws StepLimitException {
		Settings.testMethod = "runAssignment_shouldRun_noFailures";
		AppJavaClassPath appClassPath = constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(10, result.getMainTrace().size());
		
		// check for 0 written to a.
		TraceNode writtenToANode = mainTrace.getTraceNode(6);
		List<VarValue> writtenVariables = writtenToANode.getWrittenVariables();
		VarValue writtenToA = writtenVariables.get(0);
		assertEquals("0", writtenToA.getStringValue());
		
		// check for a written to b.
		TraceNode writtenToBNode = mainTrace.getTraceNode(7);
		VarValue writtenToB = writtenToBNode.getWrittenVariables().get(0);
		assertEquals("0", writtenToB.getStringValue());
		VarValue readA = writtenToBNode.getReadVariables().get(0);
		assertEquals("0", readA.getStringValue());
	}
	
	@Test
	public void testWhileLoop() throws StepLimitException {
		Settings.testMethod = "runWhileLoop_shouldRun_noFailures";
		AppJavaClassPath appClassPath = constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(30, result.getMainTrace().size());

		// check read and written variables in loop body
		TraceNode writtenToBNode = mainTrace.getTraceNode(12);
		List<VarValue> writtenVariables = writtenToBNode.getWrittenVariables();
		VarValue writtenToB = writtenVariables.get(0);
		assertEquals("0", writtenToB.getStringValue());
		
		TraceNode incrementANode = mainTrace.getTraceNode(13);
		VarValue readA = incrementANode.getReadVariables().get(0);
		assertEquals("1", readA.getStringValue());
		VarValue writtenToA = incrementANode.getWrittenVariables().get(0);
		assertEquals("2", writtenToA.getStringValue());
		
		// check for failure in loop condition
		TraceNode whileLoopBreakNode = mainTrace.getTraceNode(27);
		VarValue readAInCondition = whileLoopBreakNode.getReadVariables().get(0);
		assertEquals("5", readAInCondition.getStringValue());
		assertNotEquals(25, whileLoopBreakNode.getStepOverNext().getLineNumber());
	}
	
	@Test
	public void testForLoop() throws StepLimitException {
		Settings.testMethod = "runForLoop_shouldRun_noFailures";
		AppJavaClassPath appClassPath = constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(20, result.getMainTrace().size());
		
		// Check initialised var in loop
		TraceNode loopInitNode = mainTrace.getTraceNode(7);
		VarValue initI = loopInitNode.getWrittenVariables().get(0);
		assertEquals("0", initI.getStringValue());
		VarValue readI = loopInitNode.getReadVariables().get(0);
		assertEquals("0", readI.getStringValue());
		
		// check loop body
		TraceNode incrANode = mainTrace.getTraceNode(14);
		VarValue writtenA = incrANode.getWrittenVariables().get(0);
		assertEquals("4", writtenA.getStringValue());
		VarValue readA = incrANode.getReadVariables().get(0);
		assertEquals("3", readA.getStringValue());
		
		// check loop exit
		TraceNode loopExitNode = mainTrace.getTraceNode(17);
		VarValue writtenI = loopExitNode.getWrittenVariables().get(0);
		assertEquals("5", writtenI.getStringValue());
		VarValue lastReadI = loopExitNode.getReadVariables().get(0);
		assertEquals("5", lastReadI.getStringValue());
	}
	
	@Test
	public void testForEachLoop() throws StepLimitException {
		Settings.testMethod = "runForEachLoop_shouldRun_noFailures";
		AppJavaClassPath appClassPath = constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(15, result.getMainTrace().size());
		
		// check forEach loop initialization. (write to num, read arr)
		TraceNode forEachLoopInit = mainTrace.getTraceNode(8);
		VarValue writtenToNum = null;
		for (VarValue varVal : forEachLoopInit.getWrittenVariables()) {
			if (varVal.getVariable().getSimpleName().equals("num")) {
				writtenToNum = varVal;
				break;
			}
		}
		assertEquals("0", writtenToNum.getStringValue());
		
		ArrayValue readArr = null;
		for (VarValue varVal : forEachLoopInit.getReadVariables()) {
			if (varVal.getVariable().getSimpleName().equals("arr")) {
				readArr = (ArrayValue)varVal;
				break;
			}
		}
		
		assertEquals(2, readArr.getChildren().size());
		
		
		// check forEach loop body.
		TraceNode forEachLoopBody = mainTrace.getTraceNode(11);
		VarValue writtenA = forEachLoopBody.getWrittenVariables().get(0);
		assertEquals("1", writtenA.getStringValue());
		
		for (VarValue readVar : forEachLoopBody.getReadVariables()) {
			if (readVar.getVarName().equals("a")) {
				assertEquals("0", readVar.getStringValue());
			} else {
				assertEquals("1", readVar.getStringValue());
			}
		}
		
		// check forEach loop exit.
		TraceNode loopExitNode = mainTrace.getTraceNode(12);
		assertEquals(3, loopExitNode.getReadVariables().size());
		assertEquals(1, loopExitNode.getWrittenVariables().size());
	}
	
	private AppJavaClassPath constructClassPaths(String projectPath){
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
	
	private static void checkEnvVars() {
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
	
	private static void setupSettings() {
		Settings.projectName = "sample0";
		Settings.launchClass = "sample0.junit4.OperationTest";
		Settings.isRunTest = true;
	}
}
