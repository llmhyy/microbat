/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.paper.selectivesampling;

import org.junit.Test;

/**
 * @author LLT
 *
 */
public class StudentEvaluateTest {

	@Test
	public void test1() {
		int[] ids = new int[] {1, 2, 3};
		int[] scores = new int[] {94, 60, 100};
		StudentEvaluate.evaluate(ids, scores);
	}
	
	@Test
	public void test2() {
		int[] ids = new int[] {3, 2, 1};
		int[] scores = new int[] {75, 90, 80};
		StudentEvaluate.evaluate(ids, scores);
	}
	
	@Test
	public void test4() {
		int[] ids = new int[] {99, -100, 0};
		int[] scores = new int[] {-33, 12, 0};
		StudentEvaluate.evaluate(ids, scores);
	}
}
