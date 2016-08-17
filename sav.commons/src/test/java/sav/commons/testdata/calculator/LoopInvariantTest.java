/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.calculator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author khanh
 *
 */
public class LoopInvariantTest {
	@Test
	public void test1() {
		int a = 0;
		int b = 0;
		boolean expected = true;
		boolean result = Calculator.loopInvariant(a, b);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void test2() {
		int a = 0;
		int b = 1;
		boolean expected = true;
		boolean result = Calculator.loopInvariant(a, b);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void test3() {
		int a = 1;
		int b = 0;
		boolean expected = true;
		boolean result = Calculator.loopInvariant(a, b);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void test4() {
		int a = 1;
		int b = 1;
		boolean expected = true;
		boolean result = Calculator.loopInvariant(a, b);
		
		Assert.assertEquals(expected, result);
	}
}
