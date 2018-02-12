package sav.commons.testdata.autogeneration;
import org.junit.Assert;

/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

/**
 * @author khanh
 *
 */
public class IFindMaxTest {

	public void test1(IFindMax sampleProgram) {
		int max = sampleProgram.Max();
		
		Assert.assertTrue(sampleProgram.check(max));
	}
	

	
}
