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

import sav.commons.testdata.autogeneration.FindMaxString.FindMaxStringBuilder;

/**
 * @author LLT
 *
 */
public class FindMaxUtils {
	private FindMaxUtils() {}
	
	public static <T extends Number> T findMaxByToString(T... values) {
		List<String> toStrings = new ArrayList<String>(values.length);
		for (T value : values) {
			toStrings.add(value.toString());
		}
		FindMaxStringBuilder builder = FindMaxStringBuilder
				.forStrings(toStrings.toArray(new String[toStrings.size()]));
		String max = builder.toFindMax().Max();
		return values[toStrings.indexOf(max)];
	}
}
