/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.search1;

/**
 * @author khanh
 *
 */
public class SearchIndexEqualValue {

	/**
	 * A is ascending sorted of distinct integer. Search for A[i] = i
	 * 
	 * @param A
	 * @return i
	 */
	public static int search(int[] A) {
		if (A.length == 0) {
			return -1;
		}

		int start = 0;
		int end = A.length - 1;

		if (A[start] - start > 0 || A[end] - end < 0) {
			return -1;
		}

		while (start <= end) {
			int middle = start + (end - start) / 2;
			if (A[middle] == middle) {
				return middle;
			} else if (A[middle] < middle) { //else if (A[middle] > middle) { bug here
				end = middle - 1;
			} else {
				start = middle + 1;
			}
		}

		return -1;
	}
	
	public static boolean validate(int[] A, int index){
		if(index >= 0 && index < A.length){
			return A[index] == index;
		}
		
		for(int i = 0; i < A.length; i++){
			if(A[i] == i){
				return false;
			}
		}
		
		return true;
	}
}

