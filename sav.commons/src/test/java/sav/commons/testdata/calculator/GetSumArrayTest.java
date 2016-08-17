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
public class GetSumArrayTest {

	@Test
	public void whenArrayIsEmpty() {
		int[] A = new int[]{};
		int sum = Calculator.getSumArray(A);
		Assert.assertTrue(Calculator.validateGetSumArray(A, sum));
	}
	
	@Test
	public void whenArrayHasOneElement() {
		int[] A = new int[]{1};
		int sum = Calculator.getSumArray(A);
		Assert.assertTrue(Calculator.validateGetSumArray(A, sum));
	}
	
	@Test
	public void whenArrayHasTwoElements() {
		int[] A = new int[]{1, 2};
		int sum = Calculator.getSumArray(A);
		Assert.assertTrue(Calculator.validateGetSumArray(A, sum));
	}
	
	@Test
	public void whenArrayHasThreeElements() {
		int[] A = new int[]{1, 2, 3};
		int sum = Calculator.getSumArray(A);
		Assert.assertTrue(Calculator.validateGetSumArray(A, sum));
	}
	
	@Test
	public void whenArrayHasManyElements() {
		int[] A = new int[]{1, 2, 3, 4, 5, 6, 7};
		int sum = Calculator.getSumArray(A);
		Assert.assertTrue(Calculator.validateGetSumArray(A, sum));
	}
}
