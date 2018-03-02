package microbat.codeanalysis.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import microbat.agent.TraceAgentRunner;
import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.preference.AnalysisScopePreference;
import microbat.preference.MicrobatPreference;
import sav.common.core.SavException;
import sav.common.core.utils.StringUtils;
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
	
	public InstrumentationExecutor(AppJavaClassPath appPath, String traceDir, String traceName) {
		this.appPath = appPath;
		this.traceDir = traceDir;
		this.traceName = traceName;
		agentRunner = createTraceAgentRunner();
	}
	
	private TraceAgentRunner createTraceAgentRunner() {
		String jarPath = appPath.getAgentLib();
//		agentRunner.setPrintOutExecutionTrace(true);
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		agentRunner.setPrintOutExecutionTrace(false);
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
		agentRunner.addAgentParam(AgentParams.OPT_CLASS_PATH, config.getClasspathStr());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		agentRunner.addAgentParam(AgentParams.OPT_INCLUDES,
				StringUtils.join(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR,
						(Object[]) AnalysisScopePreference.getIncludedLibs()));
		agentRunner.addAgentParam(AgentParams.OPT_EXCLUDES,
				StringUtils.join(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR,
						(Object[]) AnalysisScopePreference.getExcludedLibs()));
		agentRunner.addAgentParam(AgentParams.OPT_VARIABLE_LAYER, MicrobatPreference.getVariableValue());
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, MicrobatPreference.getStepLimit());
		return agentRunner;
	}
	
	public TraceAgentRunner prepareAgentRunner() {
		agentRunner.setTimeout(timeout);
		return agentRunner;
	}
	
	public Trace run(){
		try {
			prepareAgentRunner();
			agentRunner.precheck();
			PrecheckInfo info = agentRunner.getPrecheckInfo();
//			System.out.println(info);

			PreCheckInformation precheckInfomation = new PreCheckInformation(info.getThreadNum(), info.getStepTotal(),
					info.isOverLong(), new ArrayList<>(info.getVisitedLocs()), info.getExceedingLimitMethods());
			precheckInfomation.setPassTest(agentRunner.isTestSuccessful());
			this.setPrecheckInfo(precheckInfomation);

			System.out.println("the trace length is: " + precheckInfomation.getStepNum());
			
			if (!info.isOverLong() && info.getExceedingLimitMethods().isEmpty()) {
				return execute(precheckInfomation);
			}
		} catch (SavException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	public PreCheckInformation runPrecheck(int stepLimit) {
		try {
			/* test stepLimit */
			agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, stepLimit);
			prepareAgentRunner();
			agentRunner.precheck();
			PrecheckInfo info = agentRunner.getPrecheckInfo();
//			System.out.println(info);
			System.out.println("isPassTest: " + agentRunner.isTestSuccessful());
			PreCheckInformation result = new PreCheckInformation(info.getThreadNum(), info.getStepTotal(), info.isOverLong(),
					new ArrayList<>(info.getVisitedLocs()), info.getExceedingLimitMethods());
			result.setPassTest(agentRunner.isTestSuccessful());
			result.setTimeout(agentRunner.isUnknownTestResult());
			this.setPrecheckInfo(result);
			return precheckInfo;
		} catch (SavException e1) {
			e1.printStackTrace();
		}
		return new PreCheckInformation(-1, -1, false, new ArrayList<ClassLocation>(), new ArrayList<String>());
	}
	
	public Trace execute(PreCheckInformation info) {
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
			return result.getTrace();
		} catch (SavException e1) {
			e1.printStackTrace();
		}

		return null;
	}
	
	public static String generateTraceFilePath(String traceDir, String traceFileName) {
		return new StringBuilder(traceDir).append(File.separator).append(traceFileName).append(TRACE_DUMP_FILE_SUFFIX)
				.toString();
		// FileUtils.createNewFileInSeq(traceDir, traceName,
		// TRACE_DUMP_FILE_SUFFIX).getAbsolutePath();
	}


//	public void run() {
//		List<String> command = new ArrayList<>();
//		command.add(this.appPath.getJavaHome()+File.separator+"bin"+File.separator+"java");
//		
//		command.add("-noverify");
//		
//		command.add("-cp");
//		
//		StringBuffer buffer = new StringBuffer();
//		buffer.append(".");
//		for(String cp: appPath.getClasspaths()){
//			buffer.append(";");
//			buffer.append(cp);
//		}
//		String cp = buffer.toString();
//		command.add(cp);
//		
//		buffer = new StringBuffer();
//		buffer.append("-javaagent:");
//		//TODO set agent lib
//		buffer.append(appPath.getAgentLib());
//		buffer.append("=");
//		
//		buffer.append("launch_class=");
//		if(appPath.getOptionalTestClass()!=null){
//			buffer.append(appPath.getOptionalTestClass());
//		}
//		else{
//			buffer.append(appPath.getLaunchClass());			
//		}
//		
//		//Java Home
//		buffer.append(",");
//		buffer.append("java_home=");
//		buffer.append(appPath.getJavaHome());
//		
//		//Class Path
//		buffer.append(",");
//		buffer.append("class_path=.");
//		for(String classPath: appPath.getClasspaths()){
//			buffer.append(";");
//			buffer.append(classPath);
//		}
//		
//		//Working Directory
//		buffer.append(",");
//		buffer.append("working_dir=");
//		buffer.append(appPath.getWorkingDirectory());
//
//		//Bootstrap 
////		buffer.append(",");
////		buffer.append("bootstrap_path=.");
////		for(String path: appPath.getAgentBootstrapPathList()){
////			buffer.append(";");
////			buffer.append(path);
////		}
//		
//		String agentInfo = buffer.toString();
//		command.add(agentInfo);
//		
//		command.add(appPath.getLaunchClass());
//		
//		if(appPath.getOptionalTestMethod()!=null){
//			command.add(appPath.getOptionalTestClass());
//			command.add(appPath.getOptionalTestMethod());
//		}
//		
//		String com = createRunningCommand(command);
//		System.out.println(com);
//		
//		ProcessBuilder builder = new ProcessBuilder(command);
//		builder.directory(new File(this.appPath.getWorkingDirectory()));
//		try {
//			Process process = builder.start();
//			String output = output(process.getInputStream());
//			System.out.println(output);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private String createRunningCommand(List<String> command) {
		StringBuffer buffer = new StringBuffer();
		for(String str: command){
			buffer.append(str);
			buffer.append(" ");
		}
		
		return buffer.toString();
	}

	private static String output(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		return sb.toString();
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
}
