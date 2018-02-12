package sav.commons.testdata.calculator;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest1 {
	@Test
	public void testPassed1() {
		int x = 0;
		int y = 1;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void testPassed2() {
		int x = 1;
		int y = 1;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void testPassed3() {
		int x = 2;
		int y = 3;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void testPassed4() {
		int x = -1;
		int y = 1;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void testFailed1() {
		int x = 5;
		int y = -1;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void testFailed2() {
		int x = 5;
		int y = -4;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void testFailed3() {
		int x = 50;
		int y = 10;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void testFailed4() {
		int x = 2;
		int y = 1;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void testFailed5() {
		int x = 10;
		int y = 5;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void testFailed6() {
		int x = -2;
		int y = 1;
		int max = Calculator.getSum1(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
}
