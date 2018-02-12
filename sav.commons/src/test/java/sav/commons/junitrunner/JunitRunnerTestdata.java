/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.junitrunner;

import org.junit.Test;

/**
 * @author LLT
 *
 */
public class JunitRunnerTestdata {

	@Test
	public void test() {
		while (true) {
			System.out.println("running " + JunitRunnerTestdata.class.getSimpleName());
		}
	}
}
