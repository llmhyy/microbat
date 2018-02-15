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
public class SampleInput {
	private int x;
	private String user; 
	
	public SampleInput(int x, String user) {
		this.x = x;
		this.user = user;
	}

	public int getX() {
		return x;
	}

	public String getUser() {
		return user;
	}
	
}
