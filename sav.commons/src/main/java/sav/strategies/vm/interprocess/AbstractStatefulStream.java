/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess;

/**
 * @author LLT
 * a marker stream used to communicate with server.
 */
public class AbstractStatefulStream {
	protected volatile StreamState state;
	
	protected void ready() {
		this.state = StreamState.READY;
	}
	
	protected void waiting() {
		this.state = StreamState.WAITING;
	}
	
	public void close() {
		this.state = StreamState.CLOSED;
	}
	
	public boolean isClosed() {
		return this.state == StreamState.CLOSED;
	}

	public boolean isReady() {
		return this.state == StreamState.READY;
	}
	
	public boolean isWaiting() {
		return this.state == StreamState.WAITING;
	}
	
	public void open() {
		if (this.state == StreamState.CLOSED) {
			this.state = StreamState.WAITING;
		}
	}
}
