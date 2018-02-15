/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess;

import java.io.OutputStream;

/**
 * @author LLT
 *
 */
public abstract class TcpInputWriter extends AbstractStatefulStream {

	public abstract void setOutputStream(OutputStream outputStream);

	public final void write() {
		if (!isReady()) {
			throw new IllegalStateException("GanInputWriter is not ready!");
		}
		writeData();
		waiting(); // wait for new data
	}
	
	protected abstract void writeData();

}
