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
public class FindInRotatedSortedArrayTest {
	
	@Test
	public void test41() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{1}, 1);
		assertEquals(0, index);
	}
	
	@Test
	public void test42() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{0,1,2}, 1);
		assertEquals(1, index);
	}
	
	@Test
	public void test43() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{0,1,4}, 3);
		assertEquals(-1, index);
	}
	
	@Test
	public void test44() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{3,4,5,1,2}, 1);
		assertEquals(3, index);
	}
	
	@Test
	public void test45() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{3,4,5,1}, 1);
		assertEquals(3, index);
	}
	
	@Test
	public void test46() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{2,2,2,2,3}, 3);
		assertEquals(4, index);
	}
	
	@Test
	public void test47() {
		SimplePrograms simpleProgram = new SimplePrograms();
		int index = simpleProgram.findInRotatedSortedArray(new int[]{2,2,2,2,2}, 0);
		assertEquals(-1, index);
	}
}
