package microbat.instrumentation.runtime;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class ExecutionTracerTest {

	@Before
	public void setUp() throws Exception {
		ExecutionTracer.appJavaClassPath = null;
		ExecutionTracer.setExpectedSteps(0);
		ExecutionTracer.setStepLimit(0);
	}

	@Test
	public final void testSetExpectedSteps() {
		final int expectedSteps = 29548;
		ExecutionTracer.setExpectedSteps(expectedSteps);
		assertEquals(ExecutionTracer.expectedSteps, expectedSteps);
	}

	@Test
	public final void testSetStepLimit() {
		final int stepLimit = 8203;
		ExecutionTracer.setStepLimit(stepLimit);
		assertEquals(ExecutionTracer.stepLimit, stepLimit);
	}

	@Test
	public final void testEnterMethod() {
		final String classname = "exp.concurrency.case1.DiningPhilosophersDebug";
		final String methodSig = "exp.concurrency.case1.DiningPhilosophersDebug#main([Ljava/lang/String;)V";
		final int startLine = 15;
		final int endLine = 18;
		final String paramTypeSignCode = "I";
		final String paramTypeNamesCode = "size";
		final Object[] params = new Object[] {5};
		ExecutionTracer tracer = new ExecutionTracer(0);
		tracer.enterMethod(classname, methodSig, startLine, endLine, paramTypeSignCode, paramTypeNamesCode, params);
		Trace trace = tracer.getTrace();
		// makes a call to hitline
	}

	@Test
	public final void testExitMethod() {
		final String classname = "exp.concurrency.case1.DiningPhilosophersDebug";
		final String methodSig = "exp.concurrency.case1.DiningPhilosophersDebug#main([Ljava/lang/String;)V";
		final int line = 18;
		ExecutionTracer tracer = new ExecutionTracer(0);
		tracer.exitMethod(line, classname, methodSig);
		// assert tracking delegate and methodcallstack
	}

	@Test
	public final void test_hitInvoke() {
		final String invokeTypeSign = "exp.concurrency.case1.Philosopher";
		final String methodSig = "exp.concurrency.case1.Philosopher#start()V";
		final String residingClassName = "exp.concurrency.case1.DiningPhilosophersDebug";
		final String residingMethodSignature = "exp.concurrency.case1.DiningPhilosophersDebug#main([Ljava/lang/String;)V";
		final String paramTypeSignsCode = "";
		final String returnTypeSign = "V";
		final Object[] params = new Object[]{};
		final int line = 21;
		ExecutionTracer tracer = new ExecutionTracer(0);
		tracer._hitInvoke(null, invokeTypeSign, methodSig, params, paramTypeSignsCode, returnTypeSign,
				line, residingClassName, residingMethodSignature);
		TraceNode node = tracer.getTrace().getLatestNode();
		// assert node's invoking details
	}

	@Test
	public final void testBuildReadRelationForArrayCopy() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testBuildWriteRelationForArrayCopy() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_hitInvokeStatic() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_hitMethodEnd() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_afterInvoke() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_hitReturn() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_hitVoidReturn() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testHitLine() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_hitLine() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_hitExeptionTarget() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_writeField() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_writeStaticField() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_readField() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_readStaticField() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_writeLocalVar() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_readLocalVar() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_iincLocalVar() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_readArrayElementVar() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_writeArrayElementVar() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void test_getTracer() {
		IExecutionTracer tracer = ExecutionTracer._getTracer(true, "InvokeSample", "run", 19, 22, "", "",
				new Object[1]);
		List<String> ls = new ArrayList<>();
	}

	@Test
	public final void testGetMainThreadStore() {
		IExecutionTracer tracer = ExecutionTracer.getMainThreadStore();
		assertEquals(tracer, tracer);
	}

	@Test
	public final void testGetAllThreadStore() {
		List<IExecutionTracer> tracers = ExecutionTracer.getAllThreadStore();
		assertEquals(tracers, tracers); // TODO
	}

	@Test
	public final void testGetCurrentThreadStore() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testStopRecordingCurrendThread() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testShutdown() {
		ExecutionTracer.shutdown();
		assertEquals(ExecutionTracer.getTracingState(), TracingState.SHUTDOWN);
	}

	@Test
	public final void testDispose() {
		ExecutionTracer.dispose();
	}

	@Test
	public final void test_start() {
		ExecutionTracer.shutdown();
		assertEquals(ExecutionTracer.getTracingState(), TracingState.TEST_STARTED);
	}

	@Test
	public final void testIsShutdown() {
		ExecutionTracer.shutdown();
		assertTrue(ExecutionTracer.isShutdown());
	}

	@Test
	public final void testGetTrace() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testLock() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsLock() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testUnLock() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetThreadId() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetThreadName() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetThreadName() {
		fail("Not yet implemented"); // TODO
	}

}
