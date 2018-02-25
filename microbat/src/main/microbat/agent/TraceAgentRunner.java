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
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.SingleTimer;
import sav.common.core.utils.StopTimer;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class TraceAgentRunner extends AgentVmRunner {
	private ServerSocket serverSocket;
	private boolean isPrecheckMode = false;
	private PrecheckInfo precheckInfo;

	private Trace trace;
	private boolean isTestSuccessful = false;
	private String testFailureMessage;
	
	public TraceAgentRunner(String agentJar) {
		super(agentJar, AgentConstants.AGENT_OPTION_SEPARATOR, AgentConstants.AGENT_PARAMS_SEPARATOR);
	}
	
	public boolean precheck(VMConfiguration config) throws SavException {
		isPrecheckMode = true;
		try {
			SingleTimer timer = SingleTimer.start("Precheck");
			addAgentParam(AgentParams.OPT_PRECHECK, "true");
			File dumpFile = File.createTempFile("tracePrecheck", ".info");
			String dumpFilePath = dumpFile.getPath();
			System.out.println("Trace dumpfile: " + dumpFilePath);
			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(dumpFilePath));
			super.startAndWaitUntilStop(config);
//			System.out.println(super.getCommandLinesString(config));
			/* collect result */
			precheckInfo = PrecheckInfo.readFromFile(dumpFilePath);
			System.out.println(timer.getResult());
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} finally {
			addAgentParam(AgentParams.OPT_PRECHECK, "false");
		}
		return true;
	}
	
	public boolean runWithDumpFileOption(VMConfiguration config, String filePath) throws SavException {
		isPrecheckMode = false;
		StopTimer timer = new StopTimer("Building trace");
		timer.newPoint("Execution");
		TraceOutputReader reader = null;
		InputStream stream = null;
		try {
			File dumpFile;
			if (filePath == null) {
				dumpFile = File.createTempFile("trace", ".exec");
				dumpFile.deleteOnExit();
			} else {
				dumpFile = FileUtils.getFileCreateIfNotExist(filePath);
			}
			System.out.println("Trace dumpfile: " + dumpFile.getPath());
			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(dumpFile.getPath()));
			super.startAndWaitUntilStop(config);
//			System.out.println(super.getCommandLinesString(config));
			timer.newPoint("Read output result");
			stream = new FileInputStream(dumpFile);
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
		isPrecheckMode = false;
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
		if (isPrecheckMode) {
			throw new UnsupportedOperationException("TraceAgent has been run in precheck mode!");
		}
		return trace;
	}
	
	public PrecheckInfo getPrecheckInfo() {
		if (!isPrecheckMode) {
			throw new UnsupportedOperationException("TraceAgent has not been run in precheck mode!");
		}
		return precheckInfo;
	}
}
