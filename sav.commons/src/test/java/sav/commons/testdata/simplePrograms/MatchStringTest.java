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
public class MatchStringTest {

	@Test
	public void test29() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("a", "a");
		assertEquals(true, match);
	}
	
	@Test
	public void test30() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("a", "b");
		assertEquals(false, match);
	}
	
	@Test
	public void test31() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("ba", "b");
		assertEquals(false, match);
	}
	
	@Test
	public void test32() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("acb", "a.b");
		assertEquals(true, match);
	}
	
	@Test
	public void test33() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("", "a*");
		assertEquals(true, match);
	}
	
	@Test
	public void test34() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("a", "a*");
		assertEquals(true, match);
	}
	
	@Test
	public void test35() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("aaaaaa", "a*");
		assertEquals(true, match);
	}
	
	@Test
	public void test36() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("aaaaaab", "a*");
		assertEquals(false, match);
	}
	
	@Test
	public void test37() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("cacbbbb", ".a.b*");
		assertEquals(true, match);
	}
	
	@Test
	public void test38() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("cabbbb", ".a.b*");
		assertEquals(true, match);
	}
	
	@Test
	public void test39() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("", ".");
		assertEquals(false, match);
	}
	
	@Test
	public void test40() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("c", ".");
		assertEquals(true, match);
	}
	
	@Test
	public void test41() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean match = simpleProgram.match("c", "a*c");
		assertEquals(true, match);
	}
}
