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
public class FindMaxPrinter<T extends IFindMax> {
	private IFindMax findMax;
	
	public FindMaxPrinter(IFindMax findMax) {
		this.findMax = findMax;
	}
	
	public void print() {
		System.out.println(findMax.Max());
	}
}
