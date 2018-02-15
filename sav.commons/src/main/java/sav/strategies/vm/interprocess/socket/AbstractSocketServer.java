/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author LLT
 *
 */
public abstract class AbstractSocketServer {
	private ServerSocket server;
	private Socket client;
	private int port;
	
	public AbstractSocketServer(int port) {
		this.port = port;
	}
	
	public final void startListening() {
		try {
			server = new ServerSocket(port);
			client = server.accept();
			setupInputStream(client.getInputStream());
			setuptOutputStream(client.getOutputStream());
			execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	protected abstract void execute();

	protected abstract void setuptOutputStream(OutputStream outputStream);

	protected abstract void setupInputStream(InputStream inputStream);
	
	
}
