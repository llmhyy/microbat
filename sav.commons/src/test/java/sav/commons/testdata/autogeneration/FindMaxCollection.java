/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.autogeneration;

import java.util.Collection;

/**
 * @author LLT
 *
 */
public class FindMaxCollection implements IFindMax {
	private Collection<Integer> numbers;

	protected FindMaxCollection(Collection<Integer> num) {
		this.numbers = num;
	}

	public int Max() {
		int result = Integer.MIN_VALUE;
		for (Integer num : numbers) {
			if (result < num) {
				result = num;
			}
		}

		return result;
	}

	public boolean check(int result) {
		for (Integer num : numbers) {
			if (result < num) {
				return false;
			}
		}

		for (Integer num : numbers) {
			if (result == num) {
				return true;
			}
		}
		
		return false;
	}
	
}
