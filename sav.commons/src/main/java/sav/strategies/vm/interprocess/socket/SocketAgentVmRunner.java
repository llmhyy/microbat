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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.interprocess.TcpInputWriter;
import sav.strategies.vm.interprocess.TcpOutputReader;

/**
 * @author LLT
 *
 */
public class SocketAgentVmRunner extends AgentVmRunner {
	private static Logger log = LoggerFactory.getLogger(SocketAgentVmRunner.class);
	public static final String CONNECTION_PORT_OPTION = "port";
	private ServerSocket serverSocket;
	private TcpInputWriter inputWriter;
	private TcpOutputReader outputReader;
	
	public SocketAgentVmRunner(TcpInputWriter inputWriter, TcpOutputReader outputReader, String agentJar) {
		super(agentJar);
		this.inputWriter = inputWriter;
		this.outputReader = outputReader;
		
		int port = VMConfiguration.findFreePort();
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		}
		addAgentParam(CONNECTION_PORT_OPTION, String.valueOf(port));
	}
	
	@Override
	public boolean startVm(List<String> commands, boolean waitUntilStop) throws SavException {
		boolean result = super.startVm(commands, false);
		try {
			Socket client = serverSocket.accept();
			try {
				if (inputWriter != null) {
					inputWriter.setOutputStream(client.getOutputStream());
				}
				setuptInputStream(client.getInputStream());
			} catch (IOException e) {
				throw new SavRtException(e);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void stop() {
		try {
			if (inputWriter != null) {
				inputWriter.close();
			}
			if (outputReader != null) {
				outputReader.close();
			}
			if (serverSocket != null) {
				serverSocket.close();
			}
			process.destroy();
		} catch (IOException e) {
			log.debug(e.getMessage());
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
