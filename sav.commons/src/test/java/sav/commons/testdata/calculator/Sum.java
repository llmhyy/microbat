/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.calculator;

/**
 * @author khanh
 *
 */
public class Sum {
	private int a;
	private int b;
	public Sum(int a){
		this.a = a;
	}
	
	public int getSum(int x, int y) {
		if(a >= 50){
			x++;
		}
		
		return x+y;
	}
	
	public static boolean validateGetSum(int x, int y, int max) {
		return (max == x + y);
	}
	
}
