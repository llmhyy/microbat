/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.junit;

import sav.common.core.utils.StringUtils;


/**
 * @author LLT
 *
 */
public class TestResult {
	public static final TestResult PASS = new TestResult(true,
			StringUtils.EMPTY);
	private boolean isPass;
	private String failureTrace;
	
	private TestResult(boolean isPass, String failureTrace) {
		this.isPass = isPass;
		this.failureTrace = failureTrace;
	}

	public boolean isPass() {
		return isPass;
	}
	
	public static TestResult fail(String failureTrace) {
		return new TestResult(false, failureTrace);
	}

	public static TestResult of(boolean isPass, String failureTrace) {
		if (isPass) {
			return PASS;
		}
		return fail(failureTrace); 
	}

	public String getFailureTrace() {
		return failureTrace;
	}

	@Override
	public String toString() {
		return "TestResult [isPass=" + isPass + ", failureTrace="
				+ failureTrace + "]";
	}
	
}
