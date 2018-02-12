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
public class SampleProgramTestPass {

	@Test
	public void test1() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(1, 1, 1);
		
		System.out.println("run test 1");
		assertEquals(max, 1);
	}
	
	@Test
	public void test2() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(1, 2, 3);
		
		System.out.println("run test 2");
		assertEquals(max, 3);
	}
	
	@Test
	public void test3() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(3, 2, 1);
		
		System.out.println("run test 3");
		assertEquals(max, 3);
	}
	
	@Test
	public void test4() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(3, 1, 2);
		
		System.out.println("run test 4");
		assertEquals(max, 3);
	}
	
}
