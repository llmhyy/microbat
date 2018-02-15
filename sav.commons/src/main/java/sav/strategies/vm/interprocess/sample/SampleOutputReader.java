/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.sample;

import java.io.BufferedReader;
import java.io.IOException;

import sav.common.core.utils.SingleTimer;
import sav.strategies.vm.interprocess.TcpOutputReader;

/**
 * @author LLT
 *
 */
public class SampleOutputReader extends TcpOutputReader {
	private static final String OUTPUT_START_TOKEN = "@@GanOutputStart@@";
	private static final String OUTPUT_END_TOKEN = "@@GanOutputEnd@@";
	private volatile SampleOutput ganOutput;
	
	public SampleOutputReader() {
		waiting();
	}
	
	public boolean isMatched(String line) {
		return OUTPUT_START_TOKEN.equals(line);
	}

	@Override
	protected void readData(BufferedReader br) {
		String line = null;
		SampleOutput output = new SampleOutput();
		try {
			while ((line = br.readLine()) != null) {
				if (line.endsWith(OUTPUT_END_TOKEN)) {
					this.ganOutput = output;
					ready();
					return;
				}
				output.add(line);
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	public SampleOutput readOutput() {
		SingleTimer timer = SingleTimer.start("read output");
		while (isWaiting()) {
			if (timer.getExecutionTime() > 1000l) {
				System.out.println("timeout!");
				return null;
			}
		}
		SampleOutput output = ganOutput;
		ganOutput = null;
		waiting();
		return output;
	}

}
