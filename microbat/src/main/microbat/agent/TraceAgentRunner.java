package microbat.agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.output.TraceOutputReader;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.SingleTimer;
import sav.common.core.utils.StopTimer;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class TraceAgentRunner extends AgentVmRunner {
	private ServerSocket serverSocket;
	private boolean isPrecheckMode = false;
	private PrecheckInfo precheckInfo;

	private RunningInfo runningInfo;
	private boolean isTestSuccessful = false;
	private boolean unknownTestResult;
	private String testFailureMessage;
	private VMConfiguration config;
	private boolean enableSettingHeapSize = true;
	
	public TraceAgentRunner(String agentJar, VMConfiguration vmConfig) {
		super(agentJar, AgentConstants.AGENT_OPTION_SEPARATOR, AgentConstants.AGENT_PARAMS_SEPARATOR);
		this.config = vmConfig;
	}
	
	@Override
	protected void buildVmOption(CollectionBuilder<String, ?> builder, VMConfiguration config) {
		builder.appendIf("-Xmx32g", enableSettingHeapSize);
		super.buildVmOption(builder, config);
	}
	
	public boolean precheck() throws SavException {
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
			updateTestResult(precheckInfo.getProgramMsg());
			System.out.println(timer.getResult());
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} finally {
			addAgentParam(AgentParams.OPT_PRECHECK, "false");
		}
		return true;
	}
	
	public boolean runWithDumpFileOption(String filePath) throws SavException {
		isPrecheckMode = false;
		StopTimer timer = new StopTimer("Building trace");
		timer.newPoint("Execution");
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
			runningInfo = RunningInfo.readFromFile(dumpFile);
			updateTestResult(runningInfo.getProgramMsg());
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} 
		System.out.println(timer.getResultString());
		return true;
	}

	public boolean runWithSocket() throws SavException {
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
			runningInfo = new RunningInfo();
			runningInfo.setTrace(reader.readTrace());
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
	
	protected void printOut(String line) {
		if (printOutExecutionTrace) {
			System.out.println(line);
		} else if (line.startsWith(AgentConstants.PROGRESS_HEADER)) {
			String[] frags = line.split(" ");
			printProgress(Integer.valueOf(frags[1]), Integer.valueOf(frags[2]));
		}
	};
	
	private void printProgress(int size, int stepNum) {
		double progress = ((double) size) / stepNum;

		double preProgr = 0;
		if (size == 1) {
			System.out.print("progress: ");
		} else {
			preProgr = ((double) (size - 1)) / stepNum;
		}

		int prog = (int) (progress * 100);
		int preP = (int) (preProgr * 100);

		int diff = prog - preP;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < diff; i++) {
			buffer.append("=");
		}
		System.out.print(buffer.toString());

		int[] percentiles = { 10, 20, 30, 40, 50, 60, 70, 80, 90 };
		for (int i = 0; i < percentiles.length; i++) {
			int percentile = percentiles[i];
			if (preP < percentile && percentile <= prog) {
				System.out.print(prog + "%");
			}
		}
	}

	private void updateTestResult(String msg) {
		if (msg == null || msg.isEmpty()) {
			unknownTestResult = true;
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
		return runningInfo.getTrace();
	}
	
	public RunningInfo getRunningInfo() {
		return runningInfo;
	}
	
	public PrecheckInfo getPrecheckInfo() {
		if (!isPrecheckMode) {
			throw new UnsupportedOperationException("TraceAgent has not been run in precheck mode!");
		}
		return precheckInfo;
	}
	
	public void setVmConfig(VMConfiguration config) {
		this.config = config;
	}
	
	public boolean isUnknownTestResult() {
		return unknownTestResult;
	}
}
