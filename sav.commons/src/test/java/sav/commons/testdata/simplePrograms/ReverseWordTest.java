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
public class ReverseWordTest {
	@Test
	public void test23() {
		SimplePrograms simpleProgram = new SimplePrograms();
		String reverse = simpleProgram.reverseWord("hello world hello world");
		assertEquals("world hello world hello", reverse);
	}
	
	@Test
	public void test24() {
		SimplePrograms simpleProgram = new SimplePrograms();
		String reverse = simpleProgram.reverseWord("  hello world hello world  ");
		assertEquals("  world hello world hello  ", reverse);
	}
	
	@Test
	public void test25() {
		SimplePrograms simpleProgram = new SimplePrograms();
		String reverse = simpleProgram.reverseWord("");
		assertEquals("", reverse);
	}
	
	@Test
	public void test26() {
		SimplePrograms simpleProgram = new SimplePrograms();
		String reverse = simpleProgram.reverseWord("    ");
		assertEquals("    ", reverse);
	}
	
	@Test
	public void test27() {
		SimplePrograms simpleProgram = new SimplePrograms();
		String reverse = simpleProgram.reverseWord("h");
		assertEquals("h", reverse);
	}
	
	@Test
	public void test28() {
		SimplePrograms simpleProgram = new SimplePrograms();
		String reverse = simpleProgram.reverseWord(" h ");
		assertEquals(" h ", reverse);
	}
	
}
