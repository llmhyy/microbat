/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.calculator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author khanh
 *
 */
public class ExtractVariableNameIssueJunit {

	@Test
	public void whenZero(){
		int a = 0;
		int x = 1;
		int y = 2;
		int sum = new ExtractVariableNameIssue(a).getSum(x, y);
		int expect = x+y;
		
		Assert.assertEquals(expect, sum);
	}
	
	@Test
	public void whenOne(){
		int a = 1;
		int x = 1;
		int y = 2;
		int sum = new ExtractVariableNameIssue(a).getSum(x, y);
		int expect = x+y;
		
		Assert.assertEquals(expect, sum);
	}
	
	@Test
	public void whenBig(){
		int a = 100;
		int x = 1;
		int y = 2;
		int sum = new ExtractVariableNameIssue(a).getSum(x, y);
		int expect = x+y;
		
		Assert.assertEquals(expect, sum);
	}
}
