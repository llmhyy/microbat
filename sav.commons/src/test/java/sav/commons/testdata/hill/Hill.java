/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.hill;

/**
 * @author khanh
 *
 */
public class Hill {
	public static int hill(int[] v) {
        // Write your code here
        // To print results to the standard output you can use System.out.println()
        // Example: System.out.println("Hello world!");
		int length = v.length;
		int[] minX = new int[length];
		int[] maxLasts = new int[length];
		int[] minLasts = new int[length];
		
		if(v[1] > v[0]){
			minX[1] = 0;
			minLasts[1] = v[1];
			maxLasts[1] = v[1];
		}
		else{
			minX[1] = (v[0] - v[1])/2 + 1;
			minLasts[1] = v[0] - minX[1]+1;
			maxLasts[1] = v[1] + minX[1];
		}
		
		for(int i = 2; i < length; i++){
			if(minLasts[i-1] - v[i] < minX[i-1] && v[i] - maxLasts[i-1] < minX[i-1]){
				minX[i] = minX[i-1];
				minLasts[i] = Math.max(minLasts[i-1] + 1, v[i] - minX[i]);
				maxLasts[i] = v[i] + minX[i];
			}
			else if(minLasts[i-1] - v[i] >= minX[i-1]){
				//new element + X + k
				//old min: - k
				int k = (minLasts[i-1] - v[i] - minX[i-1]) / 2 + 1;
				minX[i] = minX[i-1] + k;
				minLasts[i] = minLasts[i-1] - k + 1;
				maxLasts[i] = v[i] + minX[i];
			}
			else if(v[i] - maxLasts[i-1] >= minX[i-1]){
				minX[i] = minX[i-1];
				minLasts[i] = v[i] - minX[i-1];
				maxLasts[i] = v[i] + minX[i-1];
			}
		}
		
		return minX[length - 1];
    }
}
