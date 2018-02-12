/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.autogeneration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 *
 */
public class FindMaxList2D implements IFindMax {
	private List<List<Integer>> values;
	
	public FindMaxList2D(List<List<Integer>> values) {
		this.values = values; 
	}
	
	@Override
	public int Max() {
		List<Integer> maxList = new ArrayList<Integer>();
		for (List<Integer> subList : values) {
			FindMaxList fm = new FindMaxList(subList);
			maxList.add(fm.Max());
		}
		FindMaxList fm = new FindMaxList(maxList);	
		return fm.Max();
	}

	@Override
	public boolean check(int result) {
		for (List<Integer> sublist : values) {
			for (Integer num : sublist) {
				if (result < num) {
					return false;
				}
			}
		}
		for (List<Integer> sublist : values) {
			for (Integer num : sublist) {
				if (result == num) {
					return true;
				}
			}
		}
		return false;
	}

}
