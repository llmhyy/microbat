package sav.commons.testdata;
/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

/**
 * @author khanh
 *
 */
public class MaxDifference {
	public static void maxdiff(Integer[] v) {
        // Write your code here
        // To print results to the standard output you can use System.out.println()
        // Example: System.out.println("Hello world!");
		
		//compute max, min sum ending at i
		int[] maxSumEnding = new int[v.length];
		int[] minSumEnding = new int[v.length];
		
		maxSumEnding[0] = v[0];
		minSumEnding[0] = v[0];
		for(int i = 1; i < v.length; i++){
			maxSumEnding[i] = Math.max(v[i] + maxSumEnding[i-1], v[i]);
			minSumEnding[i] = Math.min(v[i] + minSumEnding[i-1], v[i]);
		}
		
		//computing max, min sum of sub array from 0 to i
		int currentMax = Integer.MIN_VALUE;
		int currentMin = Integer.MAX_VALUE;
		for(int i = 0; i < v.length; i++){
			if(maxSumEnding[i] < currentMax){
				maxSumEnding[i] = currentMax;
			}
			else{
				currentMax = maxSumEnding[i];
			}
			
			if(minSumEnding[i] < currentMin){
				currentMin = minSumEnding[i];
			}
			else{
				minSumEnding[i] = currentMin;
			}
		}
		
		//compute max, min sum starting at i
		int[] maxSumStarting = new int[v.length];
		int[] minSumStarting = new int[v.length];
		maxSumStarting[v.length - 1] = v[v.length - 1];
		minSumStarting[v.length - 1] = v[v.length - 1];
		
		for(int i = v.length - 2; i >= 0; i--){
			maxSumStarting[i] = Math.max(v[i] + maxSumStarting[i+1], v[i]);
			minSumStarting[i] = Math.min(v[i] + minSumStarting[i+1], v[i]);
		}
		
		//compute max, min sum of subarray from i to length - 1
		//computing max, min sum of sub array from 0 to i
		currentMax = Integer.MIN_VALUE;
		currentMin = Integer.MAX_VALUE;
		for(int i = v.length - 1; i >= 0; i--){
			if(maxSumStarting[i] < currentMax){
				maxSumStarting[i] = currentMax;
			}
			else{
				currentMax = maxSumStarting[i];
			}
			
			if(minSumStarting[i] < currentMin){
				currentMin = minSumStarting[i];
			}
			else{
				minSumStarting[i] = currentMin;
			}
		}
		
		int maxDifference = Integer.MIN_VALUE;
		for(int i = 0; i < v.length - 1; i++){
			maxDifference = Math.max(maxDifference, maxSumStarting[i+1] - minSumEnding[i]);
			
		}
		System.out.println(maxDifference);
				
        
    }
}
