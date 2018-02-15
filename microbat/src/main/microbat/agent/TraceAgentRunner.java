package microbat.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import microbat.instrumentation.AgentParams;
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
		try {
			Socket client = serverSocket.accept();
			try {
				InputStream inputStream = client.getInputStream();
				final InputStreamReader streamReader = new InputStreamReader(inputStream);
				// TODO: parse Trace
				BufferedReader br = new BufferedReader(streamReader);
				try {
					String line = null;
					try {
						while ((line = br.readLine()) != null) {
							
							System.out.println(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} finally {
					IOUtils.closeQuietly(streamReader);
					IOUtils.closeQuietly(br);
					IOUtils.closeQuietly(inputStream);
				}
			} catch (IOException e) {
				throw new SavRtException(e);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public Trace getTrace() {
		return trace;
	}
}
