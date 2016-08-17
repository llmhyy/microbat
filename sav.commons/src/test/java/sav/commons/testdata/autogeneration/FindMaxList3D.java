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
public class FindMaxList3D implements IFindMax {
	private List<List<List<Integer>>> values;
	
	public FindMaxList3D(List<List<List<Integer>>> values) {
		this.values = values; 
	}
	
	@Override
	public int Max() {
		List<Integer> maxList = new ArrayList<Integer>();
		for (List<List<Integer>> subList : values) {
			for (List<Integer> subsubList : subList) {
				FindMaxList fm = new FindMaxList(subsubList);
				maxList.add(fm.Max());
			}
		}
		FindMaxList fm = new FindMaxList(maxList);	
		return fm.Max();
	}

	@Override
	public boolean check(int result) {
		for (List<List<Integer>> sublist : values) {
			for (List<Integer> subsublist : sublist) {
				for (Integer num : subsublist) {
					if (result < num) {
						return false;
					}
				}
			}
		}
		for (List<List<Integer>> sublist : values) {
			for (List<Integer> subsublist : sublist) {
				for (Integer num : subsublist) {
					if (result == num) {
						return false;
					}
				}
			}
		}
		return false;
	}
}
