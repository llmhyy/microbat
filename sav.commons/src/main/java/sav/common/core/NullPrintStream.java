/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core;

import sav.common.core.iface.IPrintStream;

/**
 * @author LLT
 * 
 */
public class NullPrintStream extends AbstractPrintStream {
	private NullPrintStream() {
		
	}

	public void print(byte b) {
	}

	public void print(char c) {
	}

	public void print(double d) {
	}

	public void print(String s) {
	}

	public void println(String s) {
	}

	public void println(Object[] e) {
	}

	public static IPrintStream instance() {
		return new NullPrintStream();
	}

}
