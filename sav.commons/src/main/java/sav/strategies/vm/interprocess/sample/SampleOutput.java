/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.sample;

/**
 * @author LLT
 *
 */
public class SampleOutput {
	private StringBuffer sb = new StringBuffer();

	public void add(String line) {
		sb.append(line).append("\n");
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}
