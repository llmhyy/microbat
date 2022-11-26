package microbat.trace;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import microbat.codeanalysis.runtime.StepLimitException;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class ControlDominatorTraceTest {
	private static final String TEST_CLASS = "sample0.junit4.ControlDominationTest";
	@BeforeClass
	public static void beforeClassSetUp() {
		TraceTestHelper.checkEnvVars();
	}
	
	@Test
	public void testIfStmtControlDom() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testIfStmtControlDomination");
		assertEquals(13, mainTrace.size());
		
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
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testWhileLoopControlDomination");
		assertEquals(18, mainTrace.size());

		// Control dominator of vars in body
		TraceNode writtenToBNode = mainTrace.getTraceNode(12);
		TraceNode controlDom = writtenToBNode.getControlDominator();
		assertEquals(11, controlDom.getOrder());
		
		TraceNode incrementANode = mainTrace.getTraceNode(13);
		controlDom = incrementANode.getControlDominator();
		assertEquals(11, controlDom.getOrder());
		
		// Last stmt should have no control dom
		TraceNode whileLoopBreakNode = mainTrace.getTraceNode(15);
		controlDom = whileLoopBreakNode.getControlDominator();
		assertNull(controlDom.getOrder());
	}
	
	@Test
	public void testForLoopControlDom() throws StepLimitException {
		Trace mainTrace = TraceTestHelper.createTrace("sample0", TEST_CLASS, "testForLoopControlDomination");
		assertEquals(18, mainTrace.size());

		// Control dominator of vars in body
		TraceNode writtenToBNode = mainTrace.getTraceNode(12);
		TraceNode controlDom = writtenToBNode.getControlDominator();
		assertEquals(11, controlDom.getOrder());
		
		TraceNode incrementANode = mainTrace.getTraceNode(13);
		controlDom = incrementANode.getControlDominator();
		assertEquals(11, controlDom.getOrder());
		
		// Last stmt should have no control dom
		TraceNode whileLoopBreakNode = mainTrace.getTraceNode(15);
		controlDom = whileLoopBreakNode.getControlDominator();
		assertNull(controlDom.getOrder());
	}
}
