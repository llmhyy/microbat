/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.candies;

/**
 * @author khanh
 *
 */
public class CandiesOrg {
	public static long computeMinCandies(int[] ratings){
		int length = ratings.length;
		int[] candies = new int[length];
		candies[0] = 1;
		
		int run = 0;
		while(run + 1 < length){
			if(ratings[run] < ratings[run+1]){
				while(run+1 < length && ratings[run] < ratings[run+1]){
					candies[run+1] = candies[run] + 1;
					run++;
				}
			}
			else if(ratings[run] == ratings[run+1]){
				while(run+1 < length && ratings[run] == ratings[run+1]){
					candies[run+1] = 1;
					run++;
				}
			}
			else{
				int count = 0;
				int backupRun = run;
				while(run+1 < length && ratings[run] > ratings[run+1]){
					count++;
					run++;
				}
				candies[backupRun] = Math.max(candies[backupRun], count+1);
				
				for(int i = backupRun+1; i <= run; i++){
					candies[i] = run - i + 1;
				}
			}
		}
		
		//
		long sum = 0;
		for(int num : candies){
			sum += num;
		}
		
		return sum;
	}
}
