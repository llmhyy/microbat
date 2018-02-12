/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.autogeneration;

/**
 * @author khanh
 *
 */
public class FindMaxNums implements IFindMax{
	private int a ;
	private int b;
	private int c;
	
	public FindMaxNums(int a, int b, int c){
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public int Max()
	{
		int result = a;
		
		if(b > result)
		{
			result = b;
		}
		
		if(c > result)
		{
			result = c;
		}
		return result;
	}

	public boolean check(int result){
		if (a > result || b > result || c > result) {
			return false;
		}
		
		if(result != a && result != b && result != c){
			return false;
		}
		return true;
	}
}
