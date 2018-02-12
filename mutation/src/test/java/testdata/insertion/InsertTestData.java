/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package testdata.insertion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 * 
 */
public class InsertTestData {
	
	private int a;
	public boolean isPalindrome1(int x) {
		List<Integer> digits = new ArrayList<Integer>();
		while (x > 0) {
			int digit = x % 10;
			digits.add(digit);
			x = x / 10;
		}
		int i = 0;
		int j = digits.size();
		while (i < j) {
			if (digits.get(i++) != digits.get(--j)) {
				return false;
			}
		}
		return true;
	}
	
	public InsertTestData getThis(){
		return this;
	}

	public int getA() {
		return a;
	}	
	
	public String concat(String a, String b) {
		return a.concat(b);
	}
	
	public void specialConditionStmt() {
		if (a == 3)
			a = a + 7;
		if (a == 4) { a = a + 6;
			;
		}
		if (a == 5) 
			a = a + 1; a = a + 4;
		System.out.println(a);
	}
	
	public static void main(String[] args) {
		InsertTestData data = new InsertTestData();
		data.specialConditionStmt();
	}
}
