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
import java.util.Map;

/**
 * @author LLT
 *
 */
public class FindMaxComplexMap implements IFindMax {
	private Map<Integer, List<Integer>> values;
	
	public FindMaxComplexMap(Map<Integer, List<Integer>> values) {
		this.values = values;
	}

	@Override
	public int Max() {
		List<Integer> maxList = new ArrayList<Integer>();
		for (List<Integer> subList : values.values()) {
			FindMaxList fm = new FindMaxList(subList);
			maxList.add(fm.Max());
		}
		FindMaxList fm = new FindMaxList(maxList);
		return fm.Max();
	}

	@Override
	public boolean check(int result) {
		return false;
	}

}
