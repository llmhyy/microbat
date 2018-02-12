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
public abstract class AbstractPrintStream implements IPrintStream {
	
	public AbstractPrintStream writeln(String msg) {
		println(msg);
		return this;
	}
	
	@Override
	public void printf(String format, Object... args) {
		println(String.format(format, args));
	}
}
