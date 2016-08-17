/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm;

import java.io.IOException;
import java.util.Map;

import sav.common.core.ModuleEnum;
import sav.common.core.SavException;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

/**
 * @author LLT
 *
 */
@SuppressWarnings("restriction")
public class VMListener {
	private static final int TIMEOUT = 5000;
	private ListeningConnector connector;
	private Map<String, Argument> args;
	
	public void startListening(VMConfiguration config) throws SavException {
		connector = getConnector(config);
		try {
			connector.startListening(args);
		} catch (IOException e) {
			throw new SavException(ModuleEnum.JVM, e);
		} catch (IllegalConnectorArgumentsException e) {
			throw new SavException(ModuleEnum.JVM, e);
		}
	}
	
	public void stopListening() throws SavException {
		try {
			connector.stopListening(args);
		} catch (IOException e) {
			throw new SavException(ModuleEnum.JVM, e);
		} catch (IllegalConnectorArgumentsException e) {
			throw new SavException(ModuleEnum.JVM, e);
		}
	}

	public ListeningConnector getConnector(VMConfiguration config)
			throws SavException {
		ListeningConnector connector = getListeningConnector();
		args = connector.defaultArguments();
		specifyArguments(args, config.getPort(), TIMEOUT);
		return connector;
	}

	private void specifyArguments(Map<?, ?> map, int portNumber, int timeout) {
		Connector.IntegerArgument port= (Connector.IntegerArgument) map.get("port");
		port.setValue(portNumber);
		Connector.IntegerArgument timeoutArg= (Connector.IntegerArgument) map.get("timeout"); 
		if (timeoutArg != null) {
			timeoutArg.setValue(timeout);
		}
	}

	private ListeningConnector getListeningConnector() {
		//VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
		VirtualMachineManager vmManager = BootstrapPlugin.virtualMachineManager();
		for (Connector connector : vmManager.listeningConnectors()) {
			if ("com.sun.jdi.SocketListen".equals(connector.name())) {
				return (ListeningConnector) connector;
			}
		}
		throw new IllegalStateException();
	}

	public VirtualMachine connect(Process process)
			throws IllegalConnectorArgumentsException, SavException {
		long start = System.currentTimeMillis();
		ConnectionRunnable runnable = new ConnectionRunnable();
		Thread connectionThread = new Thread(runnable);
		connectionThread.setDaemon(true);
		connectionThread.start();
		while (connectionThread.isAlive() && !isTimeout(start)) {
			// check if process is terminated or not
			try {
				process.exitValue();
				try {
					connector.stopListening(args);
				} catch (IOException e) {
					// expected
				}
			} catch (IllegalThreadStateException ex) {
				// means: not yet terminated
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		if (connectionThread.isAlive()) {
			// timeout.
			connectionThread.interrupt();
		}
		Exception ex = runnable.getException();
		if (ex != null) {
			throw new SavException(ModuleEnum.JVM, ex);
		}
		VirtualMachine vm = runnable.getVirtualMachine();
		if (vm != null) {
			return vm;
		}
		return null;
	}

	private boolean isTimeout(long start) {
		return System.currentTimeMillis() - start > TIMEOUT;
	}
	
	private class ConnectionRunnable implements Runnable {
		private Exception ex;
		private VirtualMachine vm;

		@Override
		public void run() {
			try {
				vm = connector.accept(args);
			} catch (IOException e) {
				ex = e;
			} catch (IllegalConnectorArgumentsException e) {
				ex = e;
			}
		}
		
		public VirtualMachine getVirtualMachine() {
			return vm;
		}

		public Exception getException() {
			return ex;
		}
	}
}