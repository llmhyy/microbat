/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess;

import java.io.BufferedReader;

/**
 * @author LLT
 *
 */
public abstract class TcpOutputReader extends AbstractStatefulStream {

	public abstract boolean isMatched(String line);

	public synchronized void read(BufferedReader br) {
		synchronized (state) {
			waiting();
			readData(br);
			ready();
		}
	}

	protected abstract void readData(BufferedReader br);
}
