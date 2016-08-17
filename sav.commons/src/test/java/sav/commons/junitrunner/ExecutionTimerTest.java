/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.junitrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import sav.common.core.utils.ExecutionTimer;

/**
 * @author LLT
 *
 */
public class ExecutionTimerTest {
	
	@Test
	public void testLoop() {
		ExecutionTimer timer = new ExecutionTimer(2, TimeUnit.SECONDS);
		final List<String> result = new ArrayList<String>();
		timer.run(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true) {
						System.out.println("running testLoop");
					}
				} finally {
					result.add("testLoop stopped");
				}
			}
		});
		
		System.out.println("finish");
		Assert.assertFalse(result.isEmpty());
	}
	
	@Test
	public void testNoLoop() {
		long start = System.currentTimeMillis();
		ExecutionTimer timer = new ExecutionTimer(10, TimeUnit.SECONDS);
		timer.run(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("runing testNoLoop");
			}
		});
		System.out.println("finish");
		Assert.assertTrue(System.currentTimeMillis() - start < 10000);
	}
}
