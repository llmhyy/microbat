/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author LLT
 *
 */
public class SampleTestCase extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(SampleTestCase.class);
		return suite;
	}

	public void test1() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(1, 1, 1);
		
		System.out.println("run test 1");
		assertEquals(max, 1);
	}
	
	public void test2() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(1, 2, 3);
		
		System.out.println("run test 2");
		assertEquals(max, 3);
	}
	
	public void test3() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(3, 2, 1);
		
		System.out.println("run test 3");
		assertEquals(max, 3);
	}
	
	public void test4() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(3, 1, 2);
		
		System.out.println("run test 4");
		assertEquals(max, 3);
	}
	
	public void test5() {
		SamplePrograms sampleProgram = new SamplePrograms();
		int max = sampleProgram.Max(1, 3, 2);
		
		System.out.println("run test 5");
		assertEquals(max, 3);
	}
}
