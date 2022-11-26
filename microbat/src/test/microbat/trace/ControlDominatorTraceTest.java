package microbat.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.VarValue;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class ControlDominatorTraceTest {
	@BeforeClass
	public static void beforeClassSetUp() {
		TraceTestHelper.checkEnvVars();
		setupSettings();
	}
	
	private static void setupSettings() {
		TraceTestHelper.setupSettings();
		Settings.launchClass = "sample0.junit4.ControlDominationTest";
	}
	
	@Test
	public void testIfStmtControlDom() throws StepLimitException {
		Settings.testMethod = "testIfStmtControlDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(14, result.getMainTrace().size());
		
		// b dominated by if statement
		TraceNode writtenToBNode = mainTrace.getTraceNode(9);
		TraceNode controlDom = writtenToBNode.getControlDominator();
		assertEquals(8, controlDom.getOrder());
		
		// Last stmt not dominated by if statement. 
		// FIXME: This is a bug. It should be null, but the if statement is the control dominator.
		TraceNode lastWrittenToBNode = mainTrace.getTraceNode(11);
		assertNull(lastWrittenToBNode.getControlDominator());
	}
	
	@Test
	public void testWhileLoopControlDom() throws StepLimitException {
		Settings.testMethod = "testWhileLoopControlDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

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
	public void testForLoopControlDom() throws StepLimitException {
		Settings.testMethod = "testForLoopControlDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

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
	public void testAssignmentDataDom() throws StepLimitException {
		Settings.testMethod = "testAssignmentDataDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

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

}
