/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.junitrunner;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import sav.common.core.utils.JunitUtils;
import sav.commons.AbstractTest;
import sav.strategies.junit.JunitResult;
import sav.strategies.junit.JunitRunner;
import sav.strategies.junit.JunitRunnerParameters;

/**
 * @author LLT
 * 
 */
public class JunitRunnerTest extends AbstractTest {

	@Test
	public void testLoop() throws Exception {
		JunitRunnerParameters params = new JunitRunnerParameters();
		params.setTimeout(3, TimeUnit.SECONDS);
		params.setClassMethods(JunitUtils.extractTestMethods(Arrays
				.asList(JunitRunnerTestdata.class.getName()), null));
		JunitResult result = JunitRunner.runTestcases(params);
		System.out.println(result);
	}

}
