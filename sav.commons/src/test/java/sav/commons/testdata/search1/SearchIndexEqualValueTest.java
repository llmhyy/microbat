/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.search1;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author khanh
 *
 */
public class SearchIndexEqualValueTest {

	@Test
	public void whenThereExistsTheIndexInMiddle() {
		int [] A = new int[]{-1, 0, 1, 3, 6, 8};
		
		int expected = 3;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void whenThereExistsTheIndexInMostLeft() {
		int [] A = new int[]{0, 2, 3, 4, 5, 6};
		
		int expected = 0;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void whenThereExistsTheIndexInMostRight() {
		int [] A = new int[]{-1, 0, 1, 2, 3, 5};
		
		int expected = 5;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void whenAllElementsGreaterThanIndex() {
		int [] A = new int[]{1, 3, 5, 7, 8, 10};
		
		int expected = -1;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void whenAllElementsSmallerThanIndex() {
		int [] A = new int[]{-10, -8, -7, -5, -2};
		
		int expected = -1;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void whenNotExists() {
		int [] A = new int[]{-3, -1, 3, 5, 8, 10};
		
		int expected = -1;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void whenThereAreMoreThanOneIndexes() {
		int [] A = new int[]{-1, 0, 2, 3, 6, 8};
		
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertTrue(result == 2 || result == 3);
	}
	
	@Test
	public void whenArrayEmpty() {
		int [] A = new int[]{};
		
		int expected = -1;
		int result = SearchIndexEqualValue.search(A);
		
		Assert.assertEquals(expected, result);
	}
}
