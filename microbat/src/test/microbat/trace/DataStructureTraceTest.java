package microbat.trace;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import microbat.codeanalysis.runtime.StepLimitException;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class DataStructureTraceTest {

	private static final String TEST_CLASS = "sample0.junit4.DataStructureTest";
	@BeforeClass
	public static void beforeClassSetUp() {
		TraceTestHelper.checkEnvVars();
	}
	
	@Test
	public void testArrays() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testRunArray");
		assertEquals(21, mainTrace.size());
		
		// check for array initialization
		Set<Integer> expectedValsInArray = new HashSet<>();
		expectedValsInArray.add(0);
		expectedValsInArray.add(1);
		expectedValsInArray.add(3);
		expectedValsInArray.add(4);
		expectedValsInArray.add(5);
		Set<Integer> actualValsInArray = new HashSet<>();
		TraceNode arrayInitNode = mainTrace.getTraceNode(6);
		for (VarValue writtenVal : arrayInitNode.getWrittenVariables()) {
			if (writtenVal.getVarName().equals("arr")) {
				assertEquals(5, writtenVal.getChildren().size());
			} else {
				actualValsInArray.add(Integer.valueOf(writtenVal.getStringValue()));
			}
		}
		
		assertEquals(expectedValsInArray, actualValsInArray);
		
		// check for array access
		TraceNode arrayAccessNode = mainTrace.getTraceNode(12);		
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
		TraceNode arrayModificationNode = mainTrace.getTraceNode(18);
		VarValue writtenVal = arrayModificationNode.getWrittenVariables().get(0);
		assertEquals("2", writtenVal.getStringValue());
		VarValue readVal = arrayModificationNode.getReadVariables().get(0);
		assertEquals("arr", readVal.getVarName());
	}
	
	@Test
	public void testArrayList() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testRunArrayList");
		assertEquals(21, mainTrace.size());

		// array list initialization
		TraceNode initListNode = mainTrace.getTraceNode(6);
		VarValue initList = initListNode.getWrittenVariables().get(0);
		assertEquals(2, initList.getChildren().size());
		
		// add to array list
		TraceNode addToListNode = mainTrace.getTraceNode(8);
		VarValue listNode = addToListNode.getReadVariables().get(0);
		assertEquals(2, listNode.getChildren().size());

		// read from array list
		TraceNode readFromListNode = mainTrace.getTraceNode(13);
		for (VarValue readVar : readFromListNode.getReadVariables()) {
			if (readVar.getVarName().equals("i")) {
				assertEquals("1", readVar.getStringValue());
			}
		}

		VarValue writtenVal = readFromListNode.getWrittenVariables().get(0);
		assertEquals("2", writtenVal.getStringValue());
		
		// element removal
		TraceNode removalFromListNode = mainTrace.getTraceNode(17);
		assertEquals(0, removalFromListNode.getWrittenVariables().size());
		assertEquals(1, removalFromListNode.getReadVariables().size());
		
		// set element
		TraceNode setToListNode = mainTrace.getTraceNode(18);
		assertEquals(0, setToListNode.getWrittenVariables().size());
		assertEquals(1, setToListNode.getReadVariables().size());
	}
	
	@Test
	public void testHashMap() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testRunHashMap");
		assertEquals(28, mainTrace.size());

		
		// initialization
		TraceNode initMapNode = mainTrace.getTraceNode(6);
		VarValue initMap = initMapNode.getWrittenVariables().get(0);
		assertEquals(5, initMap.getChildren().size());
		
		// put
		TraceNode addToMapNode = mainTrace.getTraceNode(8);
		VarValue mapNode = addToMapNode.getReadVariables().get(0);
		assertEquals(7, mapNode.getChildren().size());

		// get
		TraceNode getFromMapNode = mainTrace.getTraceNode(17);
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
		TraceNode removalFromMapNode = mainTrace.getTraceNode(24);
		assertEquals(0, removalFromMapNode.getWrittenVariables().size());
		assertEquals(1, removalFromMapNode.getReadVariables().size());
		
		// clear
		TraceNode clearMapNode = mainTrace.getTraceNode(25);
		assertEquals(0, clearMapNode.getWrittenVariables().size());
		assertEquals(1, clearMapNode.getReadVariables().size());
	}
	
	@Test
	public void testHashSet() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testRunHashSet");
		assertEquals(25, mainTrace.size());
		
		// initialization
		TraceNode initSetNode = mainTrace.getTraceNode(6);
		VarValue initSet = initSetNode.getWrittenVariables().get(0);
		assertEquals(2, initSet.getChildren().size());
		
		// add
		TraceNode addToSetNode = mainTrace.getTraceNode(8);
		VarValue setNode = addToSetNode.getReadVariables().get(0);
		assertEquals(2, setNode.getChildren().size());
		
		// element removal
		TraceNode removalFromMapNode = mainTrace.getTraceNode(21);
		assertEquals(0, removalFromMapNode.getWrittenVariables().size());
		assertEquals(1, removalFromMapNode.getReadVariables().size());
		
		// clear
		TraceNode clearMapNode = mainTrace.getTraceNode(22);
		assertEquals(0, clearMapNode.getWrittenVariables().size());
		assertEquals(1, clearMapNode.getReadVariables().size());
	}
}
