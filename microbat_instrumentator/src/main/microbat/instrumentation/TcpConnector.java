package microbat.instrumentation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import microbat.instrumentation.trace.InstrConstants;
import microbat.model.trace.Trace;
import sav.common.core.SavRtException;

public class TcpConnector {
	private int tcpPort;
	private TcpTraceWriter inputWriter;
	private Socket server;

	public TcpConnector(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public TcpTraceWriter connect() throws Exception {
		inputWriter = new TcpTraceWriter();
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
			inputWriter.setOutputStream(server.getOutputStream());
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
	}
	
	public static class TcpTraceWriter {
		private PrintWriter pw;
		
		public void setOutputStream(OutputStream outputStream) {
			this.pw = new PrintWriter(outputStream, true);
		}

		public void writeTrace(Trace trace) {
			pw.println(InstrConstants.INSTRUMENT_RESULT);
			pw.println("Trace is collected!!!!");
			pw.flush();
		}
		
	}
}
