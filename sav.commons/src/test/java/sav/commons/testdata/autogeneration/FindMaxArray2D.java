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
public class FindMaxArray2D {
	private int[][] numbers;
	
	public FindMaxArray2D(int[][] num){
		this.numbers = num;
	}
	
	public int Max()
	{
		int result = Integer.MIN_VALUE;
		for(int i = 0; i < numbers.length; i++){
			for(int j = 0; j < numbers[i].length; j++){
				if(numbers[i][j] > result){
					result = numbers[i][j];
				}
			}
		}
		
		return result;
	}

	public boolean check(int result){
		for(int i = 0; i < numbers.length; i++){
			for(int j = 0; j < numbers[i].length; j++){
				if(numbers[i][j] > result){
					return false;
				}
			}
		}
		
		for(int i = 0; i < numbers.length; i++){
			for(int j = 0; j < numbers[i].length; j++){
				if(numbers[i][j] == result){
					return true;
				}
			}
		}
		
		return false;
	}
}
