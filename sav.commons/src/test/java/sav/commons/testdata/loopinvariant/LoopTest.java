/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.loopinvariant;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author khanh
 *
 */
public class LoopTest {

	@Test
	public void tessPass() {
		int x = 0;
		int y = 0;
		int N = 1;
		boolean result = Loop.testLoop(x, y, N);
		Assert.assertTrue(Loop.validateTestLoop(x, y, N, result));
	}
	
	@Test
	public void tessFail() {
		int x = 10;
		int y = 0;
		int N = 1;
		boolean result = Loop.testLoop(x, y, N);
		Assert.assertTrue(Loop.validateTestLoop(x, y, N, result));
	}
}
