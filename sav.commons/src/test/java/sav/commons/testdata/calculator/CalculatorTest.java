package sav.commons.testdata.calculator;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {

	@Test
	public void test1() {
		int x = 0;
		int y = 1;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void test2() {
		int x = 1;
		int y = 1;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void test3() {
		int x = 2;
		int y = 3;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void test4() {
		int x = -1;
		int y = 1;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void test5() {
		int x = 5;
		int y = -1;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void test6() {
		int x = 5;
		int y = -4;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}

	@Test
	public void test7() {
		int x = 50;
		int y = 10;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void test8() {
		int x = 2;
		int y = 1;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void test9() {
		int x = 10;
		int y = 5;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
	
	@Test
	public void test10() {
		int x = -2;
		int y = 1;
		int max = Calculator.getSum(x, y);
		Assert.assertTrue(Calculator.validateGetSum(x, y, max));
	}
}
