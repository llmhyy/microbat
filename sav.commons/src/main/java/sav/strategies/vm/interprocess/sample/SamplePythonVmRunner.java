/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.sample;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.SavException;
import sav.strategies.vm.interprocess.InterprocessVmRunner;

/**
 * @author LLT
 *
 */
public class SamplePythonVmRunner extends InterprocessVmRunner {
	
	public SamplePythonVmRunner(SampleInputWriter inputWriter, SampleOutputReader outputReader) {
		super(inputWriter, outputReader, true);
	}

	public void start() throws SavException {
		List<String> commands = new ArrayList<String>();
		commands.add("/usr/bin/python");
		commands.add("/Users/lylytran/Projects/Python/ganLoop.py");
		super.startVm(commands, false);
	}
}
