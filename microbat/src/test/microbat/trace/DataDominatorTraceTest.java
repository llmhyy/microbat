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
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class DataDominatorTraceTest {
	@BeforeClass
	public static void beforeClassSetUp() {
		TraceTestHelper.checkEnvVars();
		setupSettings();
	}
	
	private static void setupSettings() {
		TraceTestHelper.setupSettings();
		Settings.launchClass = "sample0.junit4.DataDominationTest";
	}
	
	@Test
	public void testArrayDataDom() throws StepLimitException {
		Settings.testMethod = "testArrayDataDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(13, result.getMainTrace().size());
		
		// b dominated by write to arr
		TraceNode writtenToBNode = mainTrace.getTraceNode(9);
		VarValue readIdx1VarVal = null;
		for (VarValue readVal : writtenToBNode.getReadVariables()) {
			if (readVal.getType().equals("int")) {
				readIdx1VarVal = readVal;
				break;
			}
		}
		assertEquals(8, writtenToBNode.getDataDominator(readIdx1VarVal).getOrder());
		
		// c not dominated by write to arr
		TraceNode writtenToCNode = mainTrace.getTraceNode(10);
		VarValue readIdx0VarVal = null;
		for (VarValue readVal : writtenToCNode.getReadVariables()) {
			if (readVal.getType().equals("int")) {
				readIdx0VarVal = readVal;
				break;
			}
		}
		assertEquals(null, writtenToCNode.getDataDominator(readIdx0VarVal));
	}
	
	@Test
	public void testWhileLoopDataDom() throws StepLimitException {
		Settings.testMethod = "testWhileLoopDataDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(21, result.getMainTrace().size());

		// b dominated by a in prev loop
		TraceNode writtenToBNode = mainTrace.getTraceNode(15);
		VarValue readAVarVal = null;
		for (VarValue readVal : writtenToBNode.getReadVariables()) {
			if (readVal.getType().equals("int")) {
				readAVarVal = readVal;
				break;
			}
		}
		assertEquals(13, writtenToBNode.getDataDominator(readAVarVal).getOrder());
		
		// a dominated by a in prev loop
		TraceNode writtenToANode = mainTrace.getTraceNode(16);
		readAVarVal = writtenToANode.getReadVariables().get(0);
		assertEquals(13, writtenToANode.getDataDominator(readAVarVal).getOrder());
		
		// c dominated by a in last loop
		TraceNode writtenToCNode = mainTrace.getTraceNode(18);
		readAVarVal = writtenToCNode.getReadVariables().get(0);
		assertEquals(16, writtenToCNode.getDataDominator(readAVarVal).getOrder());
	}
	
	@Test
	public void testForLoopDataDom() throws StepLimitException {
		Settings.testMethod = "testForLoopDataDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(21, result.getMainTrace().size());

		// b dominated by a in prev loop
		TraceNode writtenToBNode = mainTrace.getTraceNode(15);
		VarValue readAVarVal = null;
		for (VarValue readVal : writtenToBNode.getReadVariables()) {
			if (readVal.getType().equals("int")) {
				readAVarVal = readVal;
				break;
			}
		}
		assertEquals(13, writtenToBNode.getDataDominator(readAVarVal).getOrder());
		
		// a dominated by a in prev loop
		TraceNode writtenToANode = mainTrace.getTraceNode(16);
		readAVarVal = writtenToANode.getReadVariables().get(0);
		assertEquals(13, writtenToANode.getDataDominator(readAVarVal).getOrder());
		
		// c dominated by a in last loop
		TraceNode writtenToCNode = mainTrace.getTraceNode(18);
		readAVarVal = writtenToCNode.getReadVariables().get(0);
		assertEquals(16, writtenToCNode.getDataDominator(readAVarVal).getOrder());
	}
	
	@Test
	public void testAssignmentDataDom() throws StepLimitException {
		Settings.testMethod = "testAssignmentDataDomination";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(14, result.getMainTrace().size());
		
		// c dominated by a
		TraceNode writtenToCNode = mainTrace.getTraceNode(9);
		VarValue readAVarVal = writtenToCNode.getReadVariables().get(0);
		assertEquals(8, writtenToCNode.getDataDominator(readAVarVal).getOrder());
		
		// b dominated by c
		TraceNode writtenToBNode = mainTrace.getTraceNode(10);
		VarValue readCVarVal = writtenToBNode.getReadVariables().get(0);
		assertEquals(9, writtenToBNode.getDataDominator(readCVarVal).getOrder());
		
		// b dominated by a
		TraceNode writtenToBWithANode = mainTrace.getTraceNode(11);
		readAVarVal = writtenToBWithANode.getReadVariables().get(0);
		assertEquals(8, writtenToBWithANode.getDataDominator(readAVarVal).getOrder());
	}
}
