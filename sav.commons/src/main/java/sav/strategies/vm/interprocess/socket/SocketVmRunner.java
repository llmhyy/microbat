/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.VMRunner;
import sav.strategies.vm.interprocess.TcpInputWriter;
import sav.strategies.vm.interprocess.TcpOutputReader;

/**
 * @author LLT
 *
 */
public class SocketVmRunner extends VMRunner {
	private static Logger log = LoggerFactory.getLogger(SocketVmRunner.class);
	public static final String CONNECTION_PORT_OPTION = "-port";
	private Socket client;
	private TcpInputWriter inputWriter;
	private TcpOutputReader outputReader;
	
	public SocketVmRunner(TcpInputWriter inputWriter, TcpOutputReader outputReader) {
		this.inputWriter = inputWriter;
		this.outputReader = outputReader;
	}
	
	public boolean startVm(List<String> commands) throws SavException {
		int port = VMConfiguration.findFreePort();
		List<String> newCommands = new ArrayList<String>(commands.size() + 2);
		newCommands.add(CONNECTION_PORT_OPTION);
		newCommands.add(String.valueOf(port));
		newCommands.addAll(commands);
		boolean result = super.startVm(newCommands, false);
		connect(port);
		return result;
	}
	
	public void stop() {
		try {
			process.destroy();
			inputWriter.close();
			outputReader.close();
			client.close();
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
	}
	
	public void connect(int port) throws SavException {
		while (true) {
			try {
				client = new Socket("localhost", port);
				// TODO: set timeout!
				break;
			} catch (UnknownHostException e) {
				throw new SavRtException(e);
			} catch (IOException e) {
				throw new SavRtException(e);
			}
		}
		try {
			inputWriter.setOutputStream(client.getOutputStream());
			setuptInputStream(client.getInputStream());
		} catch (IOException e) {
			throw new SavRtException(e);
		}
	}

	private void setuptInputStream(final InputStream is) {
		new Thread(new Runnable() {
			public void run() {
				final InputStreamReader streamReader = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(streamReader);
				try {
					String line = null;
					try {
						while ((line = br.readLine()) != null) {
							if (!outputReader.isClosed() && outputReader.isMatched(line)) {
								outputReader.read(br);
							}
						}
					} catch (IOException e) {
						log.warn(e.getMessage());
					}
				} finally {
					IOUtils.closeQuietly(streamReader);
					IOUtils.closeQuietly(br);
					IOUtils.closeQuietly(is);
				}
				
			}
		}).start();		
	}
	
}
