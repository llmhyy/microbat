/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.autogeneration;

import java.util.List;

/**
 * @author LLT
 *
 */
public class FindMaxStatic {
	
	public static int Max(List<Integer> values) {
		FindMaxList fm = new FindMaxList(values);
		return fm.Max();
	}
}
