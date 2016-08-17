/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.paper;

import java.util.Arrays;

/**
 * @author LLT
 *
 */
public class TestClass {
	
	public void calculate (int[] scores) {
		if (scores.length == 0) {
			return;
		}
		int max = scores[0];
		int trueMax = scores[scores.length - 1];
		int min = scores[scores.length - 1];

//		int[] scores = new int[]{1,2,6}; //failed test case
		//int[] scores = new int[]{1,2,0}; //passed test case
		
		for (int i = 0; i < scores.length-1; i++) {
			if (max < scores[i]) {
				max = scores[i];
			}
			
			if (min > scores[i]) {
				min = scores[i];
			}
			
			if (trueMax < scores[i]) {
				trueMax = scores[i];
			}
		}
		//...
		
		int [] difference = new int[max-min+1]; 
//		int [] difference = new int[1000]; 
		
		for (int i = 0; i < scores.length; i++) {
//			System.out.println (max + "  " + scores[i] + "  min " + min);
			assert(max-scores[i] >= 0 && max - scores[i] < difference.length);
			difference[max-scores[i]]++; 
		}

		for (int i = 0; i < difference.length; i++) {
//			System.out.print (difference[i] + " ");
		}
	}
}