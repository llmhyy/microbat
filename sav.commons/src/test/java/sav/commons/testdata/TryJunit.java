/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import sav.commons.testdata.simplePrograms.org.SimpleProgramTests;

/**
 * @author LLT
 *
 */
public class TryJunit {

	@Test
	public void runTest() {
		JUnitCore core = new JUnitCore();
		core.addListener(new RunListener() {
			
			/* (non-Javadoc)
			 * @see org.junit.runner.notification.RunListener#testStarted(org.junit.runner.Description)
			 */
			@Override
			public void testStarted(Description description) throws Exception {
				super.testStarted(description);
			}
			
			@Override
			public void testRunFinished(Result result) throws Exception {
				System.out.println("on runFinished");
				super.testRunFinished(result);
			}
			
			/* (non-Javadoc)
			 * @see org.junit.runner.notification.RunListener#testFailure(org.junit.runner.notification.Failure)
			 */
			@Override
			public void testFailure(Failure failure) throws Exception {
				System.out.println("on testFailure!!");
				super.testFailure(failure);
			}
		});
		
		core.run(SimpleProgramTests.class);
	}
}
