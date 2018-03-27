package microbat.codeanalysis.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import microbat.agent.TraceAgentRunner;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.AgentParams.LogType;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.ClassLocation;
import microbat.preference.MicrobatPreference;
import sav.common.core.SavException;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.VMRunner;

public class InstrumentationExecutor {
	public final static String TRACE_DUMP_FILE_SUFFIX = ".exec";
	
	private AppJavaClassPath appPath;
	private String traceDir;
	private String traceName;
	private PreCheckInformation precheckInfo;
	private String traceExecFilePath;
	private TraceAgentRunner agentRunner;
	private long timeout = VMRunner.NO_TIME_OUT;
	
	private List<String> includeLibs = new ArrayList<>();
	private List<String> excludeLibs = new ArrayList<>();
	
	public InstrumentationExecutor(AppJavaClassPath appPath, String traceDir, String traceName, 
			List<String> includeLibs, List<String> excludeLibs) {
		this.appPath = appPath;
		this.traceDir = traceDir;
		this.traceName = traceName;
		this.includeLibs = includeLibs;
		this.excludeLibs = excludeLibs;
		
		agentRunner = createTraceAgentRunner();
	}
	
	private TraceAgentRunner createTraceAgentRunner() {
		String jarPath = appPath.getAgentLib();
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
//		agentRunner.setVmDebugPort(9595);
		config.setNoVerify(true);
		config.setJavaHome(appPath.getJavaHome());
		config.setClasspath(appPath.getClasspaths());
		config.setLaunchClass(appPath.getLaunchClass());
		config.setWorkingDirectory(appPath.getWorkingDirectory());
				
		if (appPath.getOptionalTestClass() != null) {
			config.addProgramArgs(appPath.getOptionalTestClass());
			config.addProgramArgs(appPath.getOptionalTestMethod());
			agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, appPath.getOptionalTestClass());
		} else {
			agentRunner.addAgentParam(AgentParams.OPT_ENTRY_POINT,
					appPath.getLaunchClass() + "." + "main([Ljava/lang/String;)V");
		}
		
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		agentRunner.addIncludesParam(this.includeLibs);
		agentRunner.addExcludesParam(this.excludeLibs);
		agentRunner.addAgentParam(AgentParams.OPT_VARIABLE_LAYER, MicrobatPreference.getVariableValue());
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, MicrobatPreference.getStepLimit());
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info));
		return agentRunner;
	}
	
	public TraceAgentRunner prepareAgentRunner() {
		agentRunner.setTimeout(timeout);
		return agentRunner;
	}
	
	public RunningInformation run(){
		try {
			prepareAgentRunner();
			agentRunner.precheck();
			PrecheckInfo info = agentRunner.getPrecheckInfo();
//			System.out.println(info);

			PreCheckInformation precheckInfomation = new PreCheckInformation(info.getThreadNum(), info.getStepTotal(),
					info.isOverLong(), new ArrayList<>(info.getVisitedLocs()), info.getExceedingLimitMethods(), info.getLoadedClasses());
			precheckInfomation.setPassTest(agentRunner.isTestSuccessful());
			this.setPrecheckInfo(precheckInfomation);

			System.out.println("the trace length is: " + precheckInfomation.getStepNum());
			
			if (!info.isOverLong() && info.getExceedingLimitMethods().isEmpty()) {
				RunningInformation rInfo = execute(precheckInfomation);
				rInfo.getTrace().setIncludedLibraryClasses(includeLibs);
				rInfo.getTrace().setExcludedLibraryClasses(excludeLibs);
				return rInfo;
			}
		} catch (SavException e1) {
			e1.printStackTrace();
		}
		
		return new RunningInformation("", -1, -1, null);
	}
	
	public PreCheckInformation runPrecheck(int stepLimit) {
		try {
			/* test stepLimit */
			agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, stepLimit);
			prepareAgentRunner();
			if (!agentRunner.precheck()) {
				precheckInfo = new PreCheckInformation();
				precheckInfo.setTimeout(true);
				return precheckInfo;
			}
			PrecheckInfo info = agentRunner.getPrecheckInfo();
//			System.out.println(info);
			System.out.println("isPassTest: " + agentRunner.isTestSuccessful());
			PreCheckInformation result = new PreCheckInformation(info.getThreadNum(), info.getStepTotal(), info.isOverLong(),
					new ArrayList<>(info.getVisitedLocs()), info.getExceedingLimitMethods(), info.getLoadedClasses());
			result.setPassTest(agentRunner.isTestSuccessful());
			result.setTimeout(agentRunner.isUnknownTestResult());
			this.setPrecheckInfo(result);
			return precheckInfo;
		} catch (SavException e1) {
			e1.printStackTrace();
		}
		return new PreCheckInformation(-1, -1, false, new ArrayList<ClassLocation>(), new ArrayList<String>(), new ArrayList<String>());
	}
	
	public RunningInformation execute(PreCheckInformation info) {
		try {
			prepareAgentRunner();
			agentRunner.addAgentParam(AgentParams.OPT_EXPECTED_STEP, info.getStepNum());
			traceExecFilePath = generateTraceFilePath(traceDir, traceName);
			agentRunner.runWithDumpFileOption(traceExecFilePath);
			// agentRunner.runWithSocket();
			RunningInfo result = agentRunner.getRunningInfo();
//			System.out.println(result);
			System.out.println("isExpectedStepsMet? " + result.isExpectedStepsMet());
			System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
			System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
			System.out.println("finish!");
			agentRunner.removeAgentParam(AgentParams.OPT_EXPECTED_STEP);
			result.getTrace().setAppJavaClassPath(appPath);
			RunningInformation information = new RunningInformation(result.getProgramMsg(), result.getExpectedSteps(), 
					result.getCollectedSteps(), result.getTrace());
			
			return information;
		} catch (SavException e1) {
			e1.printStackTrace();
		}

		return null;
	}
	
	public static String generateTraceFilePath(String traceDir, String traceFileName) {
		return new StringBuilder(traceDir).append(File.separator).append(traceFileName).append(TRACE_DUMP_FILE_SUFFIX)
				.toString();
	}

	public AppJavaClassPath getAppPath() {
		return appPath;
	}

	public void setAppPath(AppJavaClassPath appPath) {
		this.appPath = appPath;
	}

	public PreCheckInformation getPrecheckInfo() {
		return precheckInfo;
	}


	public void setPrecheckInfo(PreCheckInformation precheckInfo) {
		this.precheckInfo = precheckInfo;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	public String getTraceExecFilePath() {
		return traceExecFilePath;
	}

	public List<String> getIncludeLibs() {
		return includeLibs;
	}

	public void setIncludeLibs(List<String> includeLibs) {
		this.includeLibs = includeLibs;
	}

	public List<String> getExcludeLibs() {
		return excludeLibs;
	}

	public void setExcludeLibs(List<String> excludeLibs) {
		this.excludeLibs = excludeLibs;
	}
}
