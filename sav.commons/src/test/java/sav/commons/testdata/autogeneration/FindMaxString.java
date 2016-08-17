/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.autogeneration;

/**
 * @author LLT
 *
 */
public class FindMaxString {
	private String[] values;
	private boolean caseSensitive;
	
	private FindMaxString(String... values) {
		this.values = values;
	}
	
	public static class FindMaxStringBuilder {
		private FindMaxString findMax;
		
		private FindMaxStringBuilder(String... values) {
			findMax = new FindMaxString(values);
		}
		
		public static FindMaxStringBuilder forStrings(String[] values) {
			return new FindMaxStringBuilder(values);
		}
		
		public FindMaxStringBuilder caseSensitive(boolean caseSensitive) {
			findMax.caseSensitive = caseSensitive;
			return this;
		}
		
		public FindMaxString toFindMax() {
			return findMax;
		}
	}
	
	public String Max() {
		String max = null;
		for (String val : values) {
			if (compareTo(val, max) > 0) {
				max = val;
			}
		}
		return max;
	}

	private int compareTo(String val, String max) {
		if (max == null) {
			return 1;
		}
		if (caseSensitive) {
			return val.compareTo(max);
		}
		return val.compareToIgnoreCase(max);
	}
}
