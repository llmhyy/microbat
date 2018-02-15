package microbat.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.tcp.TraceOutputReader;
import microbat.instrumentation.trace.InstrConstants;
import microbat.model.trace.Trace;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class TraceAgentRunner extends AgentVmRunner {
	private ServerSocket serverSocket;
	private Trace trace;
	
	public TraceAgentRunner(String agentJar) {
		super(agentJar, InstrConstants.AGENT_OPTION_SEPARATOR, InstrConstants.AGENT_PARAMS_SEPARATOR);
	}

	@Override
	public boolean startVm(VMConfiguration config) throws SavException {
		try {
			int port = VMConfiguration.findFreePort();
			serverSocket = new ServerSocket(port);
			addAgentParam(AgentParams.OPT_TCP_PORT, String.valueOf(port));
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		}
		super.startVm(config);
//		System.out.println(super.getCommandLinesString(config));
		TraceOutputReader reader = null;
		try {
			Socket client = serverSocket.accept();
			InputStream inputStream = client.getInputStream();
			reader = new TraceOutputReader(inputStream);
			trace = reader.readTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public Trace getTrace() {
		return trace;
	}
}
