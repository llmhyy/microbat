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
public class SearchInSortingMatrix1Test {
	@Test
	public void test17() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1,3,5},{7,9,11},{13,15,17}}, 11);
		assertEquals(true, isFound);
	}
	
	@Test
	public void test18() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1,3,5},{7,9,11},{13,15,17}}, 12);
		assertEquals(false, isFound);
	}
	
	@Test
	public void test19() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1}}, 1);
		assertEquals(true, isFound);
	}
	
	@Test
	public void test20() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1}}, 0);
		assertEquals(false, isFound);
	}
	
	@Test
	public void test201() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1, 2, 4, 5}}, 1);
		assertEquals(true, isFound);
	}
	
	@Test
	public void test21() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1,3,5},{7,9,11},{13,15,17}}, 1);
		assertEquals(true, isFound);
	}
	
	@Test
	public void test22() {
		SimplePrograms simpleProgram = new SimplePrograms();
		boolean isFound = simpleProgram.searchInSortingMatrix1(new int[][]{{1,3,5},{7,9,11},{13,15,17}}, 17);
		assertEquals(true, isFound);
	}
}
