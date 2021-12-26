package microbat.agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.output.TraceOutputReader;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import microbat.preference.DatabasePreference;
import microbat.sql.DBSettings;
import microbat.trace.Reader;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.SingleTimer;
import sav.common.core.utils.StopTimer;
import sav.common.core.utils.StringUtils;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class TraceAgentRunner extends AgentVmRunner {
	private boolean allowFilterFileOpt = false;
	private ServerSocket serverSocket;
	private boolean isPrecheckMode = false;
	private PrecheckInfo precheckInfo;

	private RunningInfo runningInfo;
	private boolean isTestSuccessful = false;
	private boolean unknownTestResult;
	private String testFailureMessage;
	private VMConfiguration config;
	private boolean enableSettingHeapSize = true;

	private List<Trace> traces;

	public TraceAgentRunner(String agentJar, VMConfiguration vmConfig) {
		super(agentJar, AgentConstants.AGENT_OPTION_SEPARATOR, AgentConstants.AGENT_PARAMS_SEPARATOR);
		this.setConfig(vmConfig);
	}

	@Override
	protected void buildVmOption(CollectionBuilder<String, ?> builder, VMConfiguration config) {
		builder.appendIf("-Xmx30g", enableSettingHeapSize);
		// builder.appendIf("-Xmn10g", enableSettingHeapSize);
		builder.appendIf("-XX:+UseG1GC", enableSettingHeapSize);
		super.buildVmOption(builder, config);
	}

	public boolean precheck(String filePath) throws SavException {
		isPrecheckMode = true;
		try {
			SingleTimer timer = SingleTimer.start("Precheck");
			addAgentParam(AgentParams.OPT_PRECHECK, "true");
			File dumpFile;
			boolean toDeleteDumpFile = false;
			if (filePath == null) {
				dumpFile = File.createTempFile("tracePrecheck", ".info");
				toDeleteDumpFile = true;
			} else {
				dumpFile = FileUtils.getFileCreateIfNotExist(filePath);
			}

			String dumpFilePath = dumpFile.getPath();
			System.out.println("Precheck dumpfile: " + dumpFilePath);
			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(dumpFilePath));
			super.startAndWaitUntilStop(getConfig());
			if (this.isProcessTimeout()) {
				return false;
			}
			/* collect result */
			precheckInfo = PrecheckInfo.readFromFile(dumpFilePath);
			updateTestResult(precheckInfo.getProgramMsg());
			System.out.println(timer.getResult());
			if (toDeleteDumpFile) {
				dumpFile.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} finally {
			addAgentParam(AgentParams.OPT_PRECHECK, "false");
		}
		return true;
	}

	public boolean run(Reader reader) throws SavException {
		isPrecheckMode = false;
		String runId = UUID.randomUUID().toString();
		StopTimer timer = new StopTimer("Building trace");
		timer.newPoint("Execution");
		File dumpFile;
		try {
			boolean toDeleteDumpFile = false;
			switch (reader) {
			case FILE:
				dumpFile = File.createTempFile("trace", ".exec");
				dumpFile.deleteOnExit();
				break;
			default:
				dumpFile = DatabasePreference.getDBFile();
				break;
			}
			addAgentParam(AgentParams.OPT_TRACE_RECORDER, reader.name()); // why is reader name used for recorder
																			// option?
			addAgentParam(AgentParams.OPT_RUN_ID, runId);
			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(dumpFile.getPath()));
			addAgentParam(AgentParams.OPT_IS_INC_STORAGE, DBSettings.INC_STORAGE);
			super.startAndWaitUntilStop(getConfig()); // Trace recording
			System.out.println("|");
			timer.newPoint("Read output result");
			this.runningInfo = reader.create(runId).read(precheckInfo, dumpFile.getPath());
			updateTestResult(runningInfo.getProgramMsg());
			if (toDeleteDumpFile) {
				dumpFile.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		}
		System.out.println(timer.getResultString());
		return true;
	}

//	public boolean run01(Reader reader) throws SavException {
//		isPrecheckMode = false;
//		StopTimer timer = new StopTimer("Building trace");
//		timer.newPoint("Execution");
//		File dumpFile;
//		try {
//			boolean toDeleteDumpFile = false;
//			switch (reader) {
//			case FILE:
//				dumpFile = File.createTempFile("trace", ".exec");
//				dumpFile.deleteOnExit();
//				break;
//			default:
//				dumpFile = DatabasePreference.getDBFile();
//				break;
//			}
//			addAgentParam(AgentParams.OPT_TRACE_RECORDER, reader.name());
//			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(dumpFile.getPath()));
//			super.startAndWaitUntilStop(getConfig());
//			System.out.println("|");
//			timer.newPoint("Read output result");
//			setTraces(reader.create().ReadTraces(precheckInfo,dumpFile.getPath()));
//			if (toDeleteDumpFile) {
//				dumpFile.delete();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new SavRtException(e);
//		}
//		System.out.println(timer.getResultString());
//		return true;
//	}

	public boolean runWithDumpFileOption(String filePath) throws SavException {
		isPrecheckMode = false;
		StopTimer timer = new StopTimer("Building trace");
		timer.newPoint("Execution");
		try {
			File dumpFile;
			boolean toDeleteDumpFile = false;
			if (filePath == null) {
				dumpFile = File.createTempFile("trace", ".exec");
				dumpFile.deleteOnExit();
			} else {
				dumpFile = FileUtils.getFileCreateIfNotExist(filePath);
			}
			addAgentParam(AgentParams.OPT_DUMP_FILE, String.valueOf(dumpFile.getPath()));
			super.startAndWaitUntilStop(getConfig());
			System.out.println("|");
			timer.newPoint("Read output result");
			runningInfo = RunningInfo.readFromFile(dumpFile);
			updateTestResult(runningInfo.getProgramMsg());
			if (toDeleteDumpFile) {
				dumpFile.delete();
			}
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
		super.startVm(getConfig());
		// System.out.println(super.getCommandLinesString(config));
		TraceOutputReader reader = null;
		try {
			Socket client = serverSocket.accept();
			InputStream inputStream = client.getInputStream();
			reader = new TraceOutputReader(inputStream);
			String msg = reader.readString();
			updateTestResult(msg);
			List<Trace> traces = reader.readTrace();
			int collected = traces.stream().mapToInt(trace -> trace.size()).sum();
			runningInfo = new RunningInfo(msg, traces, precheckInfo.getStepTotal(), collected);
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

	@Override
	protected void printOut(String line, boolean error) {
		if (line.startsWith(AgentConstants.PROGRESS_HEADER)) {
			String[] frags = line.split(" ");
			printProgress(Integer.valueOf(frags[1]), Integer.valueOf(frags[2]));
		} else if (error || line.startsWith(AgentConstants.LOG_HEADER)) {
			System.out.println(line);
		}
	};

	private void printProgress(int size, int stepNum) {

		if (stepNum == 0) {
			return;
		}

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
				System.out.println(prog + "%");
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

	public Trace getMainTrace() {
		if (isPrecheckMode) {
			throw new UnsupportedOperationException("TraceAgent has been run in precheck mode!");
		}
		return runningInfo.getMainTrace();
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
		this.setConfig(config);
	}

	public boolean isUnknownTestResult() {
		return unknownTestResult;
	}

	public void addAgentParams(String opt, Collection<?> values) {
		super.addAgentParam(opt, StringUtils.join(values, AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR));
	}

	public void addIncludesParam(List<String> includeLibs) {
		addFilterParam(includeLibs, AgentParams.OPT_INCLUDES_FILE, AgentParams.OPT_INCLUDES, "includes");
	}

	public void addExcludesParam(List<String> excludeLibs) {
		addFilterParam(excludeLibs, AgentParams.OPT_EXCLUDES_FILE, AgentParams.OPT_EXCLUDES, "excludes");
	}

	public void addFilterParam(List<String> filterLibs, String fileOpt, String opt, String filterType) {
		if (filterLibs.size() > 10 && allowFilterFileOpt) {
			File filterFile;
			try {
				System.out.println(String.format("%s : %s", filterType, filterLibs));
				filterFile = File.createTempFile(filterType, "txt");
				filterFile.deleteOnExit();
				FileUtils.writeFile(filterFile.getAbsolutePath(), StringUtils.newLineJoin(filterLibs));
				addAgentParam(fileOpt, filterFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			addAgentParams(opt, filterLibs);
		}
	}

	public VMConfiguration getConfig() {
		return config;
	}

	public void setConfig(VMConfiguration config) {
		this.config = config;
	}

	/**
	 * @return the traces
	 */
	public List<Trace> getTraces() {
		return traces;
	}

	/**
	 * @param traces the traces to set
	 */
	public void setTraces(List<Trace> traces) {
		this.traces = traces;
	}
}
