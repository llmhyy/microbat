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
public class FindMaxWrapper1<T extends IFindMax> implements IFindMax {
	private T orgFindMax;
	
	public FindMaxWrapper1(T orgFindMax) {
		this.orgFindMax = orgFindMax;
	}
	
	@Override
	public int Max() {
		return orgFindMax.Max();
	}

	@Override
	public boolean check(int result) {
		return orgFindMax.check(result);
	}

}
