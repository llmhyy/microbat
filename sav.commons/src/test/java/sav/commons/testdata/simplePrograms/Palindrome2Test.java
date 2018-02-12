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
public class Palindrome2Test {
	@Test
	public void test6() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(true, simpleProgram.isPalindrome2(1));
	}
	
	@Test
	public void test7() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(false, simpleProgram.isPalindrome2(12));
	}
	
	@Test
	public void test8() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(true, simpleProgram.isPalindrome2(121));
	}
	
	@Test
	public void test9() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(false, simpleProgram.isPalindrome2(123));
	}
	
	@Test
	public void test10() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(false, simpleProgram.isPalindrome2(10000));
	}
}
