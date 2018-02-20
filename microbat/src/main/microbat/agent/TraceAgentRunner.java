package microbat.agent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.output.TraceOutputReader;
import microbat.model.trace.Trace;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.StopTimer;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class TraceAgentRunner extends AgentVmRunner {
	private ServerSocket serverSocket;
	private Trace trace;
	private boolean isTestSuccessful = false;
	private String testFailureMessage;
	
	public TraceAgentRunner(String agentJar) {
		super(agentJar, AgentConstants.AGENT_OPTION_SEPARATOR, AgentConstants.AGENT_PARAMS_SEPARATOR);
	}
	
	public boolean runWithDumpFileOption(VMConfiguration config) throws SavException {
		StopTimer timer = new StopTimer("Building trace");
		timer.newPoint("Execution");
		TraceOutputReader reader = null;
		InputStream stream = null;
		try {
			File tempFile = File.createTempFile("trace", ".exec");
			tempFile.deleteOnExit();
			System.out.println("Trace dumpfile: " + tempFile.getPath());
			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(tempFile.getPath()));
			super.startAndWaitUntilStop(config);
//			System.out.println(super.getCommandLinesString(config));
			timer.newPoint("Read output result");
			stream = new FileInputStream(tempFile);
			reader = new TraceOutputReader(new BufferedInputStream(stream));
			String msg = reader.readString();
			updateTestResult(msg);
			trace = reader.readTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(timer.getResultString());
		return true;
	}

	public boolean runWithSocket(VMConfiguration config) throws SavException {
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
			String msg = reader.readString();
			updateTestResult(msg);
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

	private void updateTestResult(String msg) {
		if (msg == null || msg.isEmpty()) {
			return;
		}
		int sIdx = msg.indexOf(";");
		isTestSuccessful = Boolean.valueOf(msg.substring(0, sIdx));
		testFailureMessage = msg.substring(sIdx + 1, msg.length()); 
	}

	public boolean isTestSuccessful() {
		return isTestSuccessful;
	}

	public String getTestFailureMessage() {
		return testFailureMessage;
	}

	public Trace getTrace() {
		return trace;
	}
}
