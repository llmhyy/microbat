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
 */
public class ExtractVariableNameIssue {
	private int a;
	private int b;
	private InnerClass inner;
	public ExtractVariableNameIssue(int a){
		this.a = a;
		inner = new InnerClass(a);
		inner.b = new InnerClass(a);
	}
	
	public int getSum(int x, int y) {
		if(inner.a > 3){
			x++;
		}
		
		return x+y;
	}
	
	public static boolean validateGetSum(int x, int y, int max) {
		return (max == x + y);
	}
	
	private static class InnerClass {
		int a;
		InnerClass b;
		
		public InnerClass(int a) {
			this.a = a;
		}
	}
}
