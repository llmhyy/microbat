/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.vmrunner.testdata;

/**
 * @author LLT
 *
 */
public class VmRunnerTestdataLoop {

	public static void main(String[] args) {
		while(true) {
			System.out.println("runnning " + VmRunnerTestdataLoop.class.getSimpleName());
		}
	}
}
