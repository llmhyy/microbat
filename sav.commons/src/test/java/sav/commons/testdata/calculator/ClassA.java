/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author khanh
 *
 */
public class ClassA {

	ClassB classB;
	
	public ClassA(int a){
		if(a > 3){
			classB = new ClassB();
		}
	}
	
	public int getSum(int x, int y) {
		if(classB != null && this.classB.b == 0){
			x++;
		}
		
		return x+y;
	}
	
	public static boolean validateGetSum(int x, int y, int max) {
		return (max == x + y);
	}
}

class ClassB{
	public int b;
	public ClassB(){
		this.b = 0;
	}
}
