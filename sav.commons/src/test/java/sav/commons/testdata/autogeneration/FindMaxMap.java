/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.autogeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author LLT
 * 
 */
public class FindMaxMap {
	private FindMaxCollection findMaxCollection;
	
	public FindMaxMap(Map<Integer, Integer> map) {
		Collection<Integer> values;
		if (map == null) {
			values = new ArrayList<Integer>();
		} else {
			values = map.values();
		}
		findMaxCollection = new FindMaxCollection(values);
	}
	
	public int Max() {
		return findMaxCollection.Max();
	}

	public boolean check(int result) {
		return findMaxCollection.check(result);
	}
}
