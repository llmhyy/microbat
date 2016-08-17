/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core;

import java.io.PrintStream;

import sav.common.core.utils.Assert;

/**
 * @author LLT
 * 
 */
public class SavPrintStream extends AbstractPrintStream {
	private PrintStream out;

	public SavPrintStream(PrintStream out) {
		Assert.notNull(out,
				"PrintStream can not be null. Use NullPrintStream instead!");
		this.out = out;
	}

	public void print(byte b) {
		if (out != null) {
			out.print(b);
		}
	}

	public void print(char c) {
		if (out != null) {
			out.print(c);
		}
	}

	public void print(double d) {
		if (out != null) {
			out.print(d);
		}
	}

	public void print(String s) {
		if (out != null) {
			out.print(s);
		}
	}

	public void println(String s) {
		if (out != null) {
			out.println(s);
		}
	}

	public void println(Object[] e) {
		if (out != null) {
			out.println(e);
		}
	}

}
