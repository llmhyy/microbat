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
public class FindMaxWrapper {
	private IFindMax findMax;
	
	private FindMaxWrapper(IFindMax findMax) {
		this.findMax = findMax;
	}
	
	public static FindMaxWrapper wrap(IFindMax findMax) {
		return new FindMaxWrapper(findMax);
	}
	
	public int Max() {
		return findMax.Max();
	}
}
