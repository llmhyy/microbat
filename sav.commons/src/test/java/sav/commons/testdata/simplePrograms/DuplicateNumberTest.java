/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.simplePrograms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * @author LLT
 *
 */
public class DuplicateNumberTest {
	
	@Test
	public void test11() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int duplicateNumber = simpleProgram.duplicatedNumber(new int[]{0});
		assertEquals(-1, duplicateNumber);
	}
	
	@Test
	public void test12() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int duplicateNumber = simpleProgram.duplicatedNumber(new int[]{0,1});
		assertEquals(-1, duplicateNumber);
	}
	
	@Test
	public void test13() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int duplicateNumber = simpleProgram.duplicatedNumber(new int[]{0,1,3,1});
		assertEquals(1, duplicateNumber);
	}
	
	@Test
	public void test14() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int duplicateNumber = simpleProgram.duplicatedNumber(new int[]{0, 2, 4, 2, 3});
		assertEquals(2, duplicateNumber);
	}
	
	@Test
	public void test15() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int duplicateNumber = simpleProgram.duplicatedNumber(new int[]{0, 4, 2, 4, 2});
		assertEquals(true, duplicateNumber == 2 || duplicateNumber == 4);
	}
	
	@Test
	public void test16() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int duplicateNumber = simpleProgram.duplicatedNumber(new int[]{0, 0, 0, 0, 0, 0, 0});
		assertEquals(0, duplicateNumber);
	}
}
