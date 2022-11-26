package microbat.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class DataStructureTraceTest {
	@BeforeClass
	public static void beforeClassSetUp() {
		TraceTestHelper.checkEnvVars();
		setupSettings();
	}
	
	private static void setupSettings() {
		TraceTestHelper.setupSettings();
		Settings.launchClass = "sample0.junit4.DataStructureTest";
	}
	
	@Test
	public void testArrays() throws StepLimitException {
		Settings.testMethod = "testRunArray";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(22, result.getMainTrace().size());
		
		// check for array initialization
		Set<Integer> expectedValsInArray = new HashSet<>();
		expectedValsInArray.add(0);
		expectedValsInArray.add(1);
		expectedValsInArray.add(3);
		expectedValsInArray.add(4);
		expectedValsInArray.add(5);
		Set<Integer> actualValsInArray = new HashSet<>();
		TraceNode arrayInitNode = mainTrace.getTraceNode(7);
		for (VarValue writtenVal : arrayInitNode.getWrittenVariables()) {
			if (writtenVal.getVarName().equals("arr")) {
				assertEquals(5, writtenVal.getChildren().size());
			} else {
				actualValsInArray.add(Integer.valueOf(writtenVal.getStringValue()));
			}
		}
		
		assertEquals(expectedValsInArray, actualValsInArray);
		
		// check for array access
		TraceNode arrayAccessNode = mainTrace.getTraceNode(13);		
		for (VarValue writtenVal : arrayAccessNode.getReadVariables()) {
			if (writtenVal.getVarName().equals("arr")) {
				assertEquals(5, writtenVal.getChildren().size());
			} else if (writtenVal.getVarName().equals("i")) {
				assertEquals("2", writtenVal.getStringValue());
			} else {
				assertEquals("5", writtenVal.getStringValue());
			}
		}
		VarValue writtenB = arrayAccessNode.getWrittenVariables().get(0);
		assertEquals("5", writtenB.getStringValue());
		
		// check for writing to array
		TraceNode arrayModificationNode = mainTrace.getTraceNode(19);
		VarValue writtenVal = arrayModificationNode.getWrittenVariables().get(0);
		assertEquals("2", writtenVal.getStringValue());
		VarValue readVal = arrayModificationNode.getReadVariables().get(0);
		assertEquals("arr", readVal.getVarName());
	}
	
	@Test
	public void testArrayList() throws StepLimitException {
		Settings.testMethod = "testRunArrayList";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(22, result.getMainTrace().size());

		// array list initialization
		TraceNode initListNode = mainTrace.getTraceNode(7);
		VarValue initList = initListNode.getWrittenVariables().get(0);
		assertEquals(2, initList.getChildren().size());
		
		// add to array list
		TraceNode addToListNode = mainTrace.getTraceNode(9);
		VarValue listNode = addToListNode.getReadVariables().get(0);
		assertEquals(2, listNode.getChildren().size());

		// read from array list
		TraceNode readFromListNode = mainTrace.getTraceNode(14);
		for (VarValue readVar : readFromListNode.getReadVariables()) {
			if (readVar.getVarName().equals("i")) {
				assertEquals("1", readVar.getStringValue());
			}
		}

		VarValue writtenVal = readFromListNode.getWrittenVariables().get(0);
		assertEquals("2", writtenVal.getStringValue());
		
		// element removal
		TraceNode removalFromListNode = mainTrace.getTraceNode(18);
		assertEquals(0, removalFromListNode.getWrittenVariables().size());
		assertEquals(1, removalFromListNode.getReadVariables().size());
		
		// set element
		TraceNode setToListNode = mainTrace.getTraceNode(19);
		assertEquals(0, setToListNode.getWrittenVariables().size());
		assertEquals(1, setToListNode.getReadVariables().size());
	}
	
	@Test
	public void testHashMap() throws StepLimitException {
		Settings.testMethod = "testRunHashMap";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(29, result.getMainTrace().size());
		
		// initialization
		TraceNode initMapNode = mainTrace.getTraceNode(7);
		VarValue initMap = initMapNode.getWrittenVariables().get(0);
		assertEquals(5, initMap.getChildren().size());
		
		// put
		TraceNode addToMapNode = mainTrace.getTraceNode(9);
		VarValue mapNode = addToMapNode.getReadVariables().get(0);
		assertEquals(7, mapNode.getChildren().size());

		// get
		TraceNode getFromMapNode = mainTrace.getTraceNode(18);
		assertEquals(2, getFromMapNode.getReadVariables().size());		
		for (VarValue readVar : getFromMapNode.getReadVariables()) {
			if (readVar.getVarName().equals("map")) {
				assertEquals(7, readVar.getChildren().size());
			} else {
				assertEquals("1", readVar.getStringValue());
			}
		}
		assertEquals(1, getFromMapNode.getWrittenVariables().size());
		assertEquals("0", getFromMapNode.getWrittenVariables().get(0).getStringValue());
		
		// element removal
		TraceNode removalFromMapNode = mainTrace.getTraceNode(25);
		assertEquals(0, removalFromMapNode.getWrittenVariables().size());
		assertEquals(1, removalFromMapNode.getReadVariables().size());
		
		// clear
		TraceNode clearMapNode = mainTrace.getTraceNode(26);
		assertEquals(0, clearMapNode.getWrittenVariables().size());
		assertEquals(1, clearMapNode.getReadVariables().size());
	}
	
	@Test
	public void testHashSet() throws StepLimitException {
		Settings.testMethod = "testRunHashSet";
		AppJavaClassPath appClassPath = TraceTestHelper.constructClassPaths(String.join(File.separator, System.getProperty("user.dir"), "src", "test", "samples", "sample0"));

		InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
				".", "trace", new ArrayList<>(), new ArrayList<>());
		final RunningInfo result = executor.run();
		Trace mainTrace = result.getMainTrace();
		assertEquals(26, result.getMainTrace().size());
		
		// initialization
		TraceNode initSetNode = mainTrace.getTraceNode(7);
		VarValue initSet = initSetNode.getWrittenVariables().get(0);
		assertEquals(2, initSet.getChildren().size());
		
		// add
		TraceNode addToSetNode = mainTrace.getTraceNode(9);
		VarValue setNode = addToSetNode.getReadVariables().get(0);
		assertEquals(2, setNode.getChildren().size());
		
		// element removal
		TraceNode removalFromMapNode = mainTrace.getTraceNode(22);
		assertEquals(0, removalFromMapNode.getWrittenVariables().size());
		assertEquals(1, removalFromMapNode.getReadVariables().size());
		
		// clear
		TraceNode clearMapNode = mainTrace.getTraceNode(23);
		assertEquals(0, clearMapNode.getWrittenVariables().size());
		assertEquals(1, clearMapNode.getReadVariables().size());
	}
}
