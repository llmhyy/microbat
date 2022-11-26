package microbat.trace;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import microbat.codeanalysis.runtime.StepLimitException;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class DataDominatorTraceTest {
	private static final String TEST_CLASS = "sample0.junit4.DataDominationTest";
	@BeforeClass
	public static void beforeClassSetUp() {
		TraceTestHelper.checkEnvVars();
	}
	
	@Test
	public void testArrayDataDom() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testArrayDataDomination");
		assertEquals(12, mainTrace.size());
		
		// b dominated by write to arr
		TraceNode writtenToBNode = mainTrace.getTraceNode(8);
		VarValue readIdx1VarVal = null;
		for (VarValue readVal : writtenToBNode.getReadVariables()) {
			if (readVal.getType().equals("int")) {
				readIdx1VarVal = readVal;
				break;
			}
		}
		assertEquals(7, writtenToBNode.getDataDominator(readIdx1VarVal).getOrder());
		
		// c not dominated by write to arr
		TraceNode writtenToCNode = mainTrace.getTraceNode(9);
		VarValue readIdx0VarVal = null;
		for (VarValue readVal : writtenToCNode.getReadVariables()) {
			if (readVal.getType().equals("int")) {
				readIdx0VarVal = readVal;
				break;
			}
		}
		assertEquals(6, writtenToCNode.getDataDominator(readIdx0VarVal).getOrder());
	}
	
	@Test
	public void testWhileLoopDataDom() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testWhileLoopDataDomination");
		assertEquals(21, mainTrace.size());

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
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testForLoopDataDomination");
		assertEquals(21, mainTrace.size());

		// b dominated by a in prev loop
		TraceNode writtenToBNode = mainTrace.getTraceNode(15);
		VarValue readAVarVal = null;
		for (VarValue readVal : writtenToBNode.getReadVariables()) {
			if (readVal.getVarName().equals("a")) {
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
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testAssignmentDataDomination");
		assertEquals(14, mainTrace.size());
		
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
