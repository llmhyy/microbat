/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.sample;

import java.io.OutputStream;
import java.io.PrintWriter;

import sav.strategies.vm.interprocess.TcpInputWriter;

/**
 * @author LLT
 *
 */
public class SampleInputWriter extends TcpInputWriter {
	private SampleInput ganInput;
	private PrintWriter pw;
	
	public SampleInputWriter() {
		waiting();
	}
	
	public void sendData(SampleInput input) {
		if (isClosed()) {
			throw new IllegalStateException("InputWriter is closed!");
		}
		this.ganInput = input;
		ready();
	}
	
	@Override
	protected void writeData() {
		pw.println(String.valueOf(ganInput.getX()));
		pw.println(ganInput.getUser());
		ganInput = null;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.pw = new PrintWriter(outputStream, true);
	}

	@Override
	public void close() {
		super.close();
		if (pw != null) {
			pw.close();
		}
	}
	
}
