/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.simplePrograms.org;

import java.util.ArrayList;
import java.util.List;

/**
 * @author khanh
 *
 */
public class SimpleProgramsCorr {

	/**
	 * Check whether a number is a palindrome
	 * 121 is a palindrome but 123 is not
	 * convert to list of digits and check
	 * @param number
	 * @return
	 */
	public boolean isPalindrome1(int number){
		List<Integer> digits = new ArrayList<Integer>();
		
		while(number > 0){
			int digit = number % 10;
			digits.add(digit);
			number = number / 10;
		}
		
		for(int i = 0; i < digits.size()/ 2; i++){
			if(digits.get(i) != digits.get(digits.size() - 1 - i)){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check whether a number is a palindrome
	 * 121 is a palindrome but 123 is not
	 * build the reverse number and check they are the same
	 * @param number
	 * @return
	 */
	public boolean isPalindrome2(int number){
		int backupNumber = number;
		
		int reverseNumber = 0;
		
		while(number > 0){
			int digit = number % 10;
			reverseNumber = reverseNumber * 10 + digit;
			number = number / 10;
		}
		
		return (reverseNumber == backupNumber);
	}
	
	/**
	 * array length n of numbers from 0-n-1
	 * return number which appears more than once
	 * @param numbers
	 * @return
	 */
	public int duplicatedNumber(int[] numbers){
		
		for(int i = 0; i < numbers.length; i++){
			while(numbers[i] != i){
				int temp = numbers[numbers[i]];
				
				if(numbers[i] == temp){
					return numbers[i];
				}
				
				numbers[numbers[i]] = numbers[i];
				numbers[i] = temp;
				
			}
		}
		
		return -1;
	}
	
	/**
	 * the matrix is sorted as 
	 * 1 3 5
		7 9 11
		13 15 17
		left to right, and the last number in each
		row is not greater than the first number of the next row
	 * @param matrix
	 * @param number
	 * @return
	 */
	public boolean searchInSortingMatrix1(int[][] matrix, int number){
		int row = matrix.length;
		int col = matrix[0].length;
		
		int start = 0;
		int end = row * col - 1;
		
		while(start <= end){
			int middle = start + (end - start) / 2;
			
			int c = middle % col;
			int r = middle / col;
			
			if(matrix[r][c] == number){
				return true;
			}
			else if(matrix[r][c] > number){
				end = middle - 1;
			}
			else{
				start = middle + 1;
			}
			
		}
		
		
		return false;
	}
	
	public String reverseWord(String s){
		char[] sentence = s.toCharArray();
		
		reverseWord(sentence, 0, sentence.length-1);
		int start = 0;
		int end = 0;
		
		char SPACE = ' ';
		
		while (start < sentence.length) {
			while (start < sentence.length && sentence[start] == SPACE) {
				start++;
			}

			end = start;
			while (end < sentence.length && sentence[end] != SPACE) {
				end++;
			}

			reverseWord(sentence, start, end - 1);
			start = end;
		}
		
		return new String(sentence);
		
	}
	
	/**
	 * Reverse the char[] from i to j
	 * @param s
	 * @param i
	 * @param j
	 */
	private void reverseWord(char[] s, int i, int j){
		if(!(i >= 0 && j <= s.length && i <= j)){
			return;
		}
		
		while(i <= j){
			char temp = s[i];
			s[i] = s[j];
			s[j] = temp;
			
			//
			i++;
			j--;
		}		
	}
	
	/**
	 * pattern can contain . for all chars, * for a sequence of the preceding char
	 * @param s
	 * @param pattern
	 * @return
	 */
	public boolean match(String s, String pattern){
		char[] sentence = s.toCharArray();
		char[] p = pattern.toCharArray();
		
		return match(sentence, 0, p, 0);
	}
	
	private boolean match(char[] s, int startS, char[] pattern, int startP){
		char DOT = '.';
		char START = '*';
		
		if(startS >= s.length && startP >= pattern.length){
			return true;
		}
		
		if(startS >= s.length && startP < pattern.length){
			return (startP + 1 < pattern.length && pattern[startP + 1] == START && match(s, startS, pattern, startP + 2));
		}
		
		if(startS < s.length && startP >= pattern.length){
			return false;
		}
		
		if(startP + 1 >= pattern.length){
			return (pattern[startP] == DOT || s[startS] == pattern[startP]) && match(s, startS + 1, pattern, startP + 1);
		}
		else if(pattern[startP + 1] == START){
			if(pattern[startP] == DOT || s[startS] == pattern[startP]){
				return match(s, startS, pattern, startP + 2) || //no char in s matches ?*
						match(s, startS+1, pattern, startP) || //one char in s matches  but still have ?*
						match(s, startS+1, pattern, startP + 2);//one char in s matches and finish .?* where ? can be . or any char
			}
			else{
				return match(s, startS, pattern, startP + 2);
			}			
		}
		else{
			return (pattern[startP] == DOT || s[startS] == pattern[startP]) && match(s, startS + 1, pattern, startP + 1);
		}	
		
	}
	
	public int findInRotatedSortedArray(int[] A, int num){
		int start = 0;
		int end = A.length - 1;
		
		while(start <= end){
			int middle = start + (end - start)/2;
			
			if(A[middle] == num){
				return middle;
			}
			
			if(A[start] <= A[middle]){
				if(num >= A[start] && num < A[middle]){
					end = middle - 1;
				}
				else{
					start = middle + 1;
				}
			}
			else{
				if(num > A[middle] && num <= A[end]){
					start = middle + 1;
				}
				else{
					end = middle - 1;
				}
			}
		}
		
		return -1;
	}
}
