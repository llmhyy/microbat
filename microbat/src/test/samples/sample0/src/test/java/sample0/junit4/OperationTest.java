package sample0.junit4;

import org.junit.Test;
import sample0.Operation;

import static org.junit.Assert.assertEquals;

public class OperationTest {
	Operation operation = new Operation();
	@Test
	public void testRunAssignment() {
		operation.runAssignment();
	}

	@Test
	public void testRunIfStmt() {
		operation.runIfStmt();
	}

	@Test
	public void testRunForLoop() {
		operation.runForLoop();
	}

	@Test
	public void testRunWhileLoop() {
		operation.runWhileLoop();
	}

	@Test
	public void testCallAnotherMethod() {
		assertEquals(2, operation.callAnotherMethod());
	}

	@Test
	public void testRunForEachLoop() {
		operation.runForEachLoop();
	}
}