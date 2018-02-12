/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm;

import sav.common.core.ModuleEnum;
import sav.common.core.SavException;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * @author LLT
 *
 */
@SuppressWarnings("restriction")
public class SimpleDebugger {
	private Process process;
	private VMRunner vmRunner;

	/**
	 * using scenario Target VM attaches to previously-running debugger.
	 */
	public VirtualMachine run(VMConfiguration config) throws SavException {
		VMListener listener = new VMListener();
		listener.startListening(config);
		try {
			vmRunner = new VMRunner();
			vmRunner.startVm(config);
			process = vmRunner.getProcess();
			if (process != null) {
				return listener.connect(process);
			}
		} catch (IllegalConnectorArgumentsException e) {
			throw new SavException(ModuleEnum.JVM, e);
		} finally {
			listener.stopListening();
		}
		return null;
	}
	
	public void waitProcessUntilStop() throws SavException {
		vmRunner.waitUntilStop(process);
	}
	
	public String getProccessError() {
		return vmRunner.getProccessError();
	}
}