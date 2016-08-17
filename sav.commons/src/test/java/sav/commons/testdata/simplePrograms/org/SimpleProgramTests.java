/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.simplePrograms.org;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import sav.commons.testdata.simplePrograms.SimplePrograms;


/**
 * @author khanh
 *
 */
public class SimpleProgramTests {
	private static int i = 1;
	
	@After
	public void tearDown() {
		System.out.println("complete method!" + i++);
	}
	
	@Test
	public void test1() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(true, simpleProgram.isPalindrome1(1));
	}
	
	@Test
	public void test2() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(false, simpleProgram.isPalindrome1(12));
	}
	
	@Test
	public void test3() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(true, simpleProgram.isPalindrome1(121));
	}
	
	@Test
	public void test4() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(false, simpleProgram.isPalindrome1(123));
	}
	
	@Test
	public void test5() {
		SimplePrograms simpleProgram = new SimplePrograms();
		assertEquals(false, simpleProgram.isPalindrome1(10000));
	}
	
	//--------------------------
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
	
	//-------------------------------------
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
	
	//--------------------
	
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
	
	//---------------------------
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
	
	//---------------------------------------------
	
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
	
	//-----------------------------------
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
