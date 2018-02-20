package microbat.instrumentation.output.tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import microbat.instrumentation.output.TraceOutputWriter;
import sav.common.core.SavRtException;

public class TcpConnector {
	private int tcpPort;
	private TraceOutputWriter inputWriter;
	private Socket server;

	public TcpConnector(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public TraceOutputWriter connect() throws Exception {
		while (true) {
			try {
				server = new Socket("localhost", tcpPort);
				// TODO: set timeout!
				break;
			} catch (UnknownHostException e) {
				throw new SavRtException(e);
			} catch (IOException e) {
				throw new SavRtException(e);
			}
		}
		try {
			inputWriter = new TraceOutputWriter(server.getOutputStream());
		} catch (IOException e) {
			throw new SavRtException(e);
		}
		return inputWriter;
	}

	public void close() {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (inputWriter != null) {
			try {
				inputWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
