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
public class FindMaxCompositionArray2D {
	private IFindMax[][] numbers;
	
	public FindMaxCompositionArray2D(IFindMax[][] num){
		this.numbers = num;
	}
	
	public int Max()
	{
		int result = Integer.MIN_VALUE;
		for(int i = 0; i < numbers.length; i++){
			for(int j = 0; j < numbers[i].length; j++){
				int maxTemp = numbers[i][j].Max();
				if(maxTemp > result){
					result = maxTemp;
				}
			}
		}
		
		return result;
	}

	public boolean check(int result){
		for(int i = 0; i < numbers.length; i++){
			for(int j = 0; j < numbers[i].length; j++){
				if(numbers[i][j].Max() > result){
					return false;
				}
			}
		}
		
		for(int i = 0; i < numbers.length; i++){
			for(int j = 0; j < numbers[i].length; j++){
				if(numbers[i][j].Max() == result){
					return true;
				}
			}
		}
		
		return false;
	}
}
