/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.python;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.strategies.vm.interprocess.InterprocessVmRunner;
import sav.strategies.vm.interprocess.TcpInputWriter;
import sav.strategies.vm.interprocess.TcpOutputReader;

/**
 * @author LLT
 *
 */
public class PythonVmRunner extends InterprocessVmRunner {

	public PythonVmRunner(TcpInputWriter inputWriter, TcpOutputReader outputReader) {
		super(inputWriter, outputReader);
	}
	
	public PythonVmRunner(TcpInputWriter inputWriter, TcpOutputReader outputReader, boolean closeStreamsOnStop) {
		super(inputWriter, outputReader, closeStreamsOnStop);
	}

	public void start(PythonVmConfiguration vmConfig) throws SavException {
		List<String> commands = buildCommandsFromConfiguration(vmConfig);
		super.startVm(commands, false);
	}

	private List<String> buildCommandsFromConfiguration(PythonVmConfiguration vmConfig) {
		CollectionBuilder<String, Collection<String>> builder = CollectionBuilder.init(new ArrayList<String>());
		builder.append(vmConfig.getPythonHome()).append(vmConfig.getLaunchClass());
		return builder.getResult();
	}
}
