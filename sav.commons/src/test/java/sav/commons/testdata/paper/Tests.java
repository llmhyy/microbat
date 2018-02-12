/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.paper;

import org.junit.Test;

/**
 * @author LLT
 * 
 */
public class Tests {

	@Test
	public void test1() {
		int[] scores = new int[] { 1, 2, 6 }; // failed test case
		TestClass testClass = new TestClass();
		testClass.calculate(scores);
	}

	@Test
	public void test2() {
		int[] scores = new int[] { -19, -6, -13 }; // passed test case
		TestClass testClass = new TestClass();
		testClass.calculate(scores);
	}
}
