/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author LLT
 *
 */
public class SampleProgramTestFail {
	@Test
	public void test5() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(1, 3, 2);
		
		System.out.println("run test 5");
		assertEquals(max, 3);
	}
	
	@Test
	public void test6() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(3, 5, 4);
		
		System.out.println("run test 6");
		assertEquals(max, 3);
	}
	
	@Test
	public void test7() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(3, 8, 2);
		
		System.out.println("run test 7");
		assertEquals(max, 8);
	}
}
