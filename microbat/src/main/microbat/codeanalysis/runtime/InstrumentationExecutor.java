package microbat.codeanalysis.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import microbat.agent.TraceAgentRunner;
import microbat.instrumentation.AgentConstants;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import microbat.preference.AnalysisScopePreference;
import microbat.preference.MicrobatPreference;
import microbat.util.Settings;
import sav.common.core.SavException;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.vm.VMConfiguration;

public class InstrumentationExecutor {
	private final static String TEST_CASE_RUNNER = "microbat.evaluation.junit.MicroBatTestRunner";
	private final static String TRACE_FILE_OUTPUT_FOLDER = "trace";
	
	private AppJavaClassPath appPath;
	private PreCheckInformation precheckInfo;
	
	public InstrumentationExecutor(AppJavaClassPath appPath){
		this.appPath = appPath;
	}
	
	
	public Trace run(){
		String jarPath = appPath.getAgentLib();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath);
//		agentRunner.setPrintOutExecutionTrace(true);
		VMConfiguration config = new VMConfiguration();
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
//		agentRunner.addAgentParam(AgentParams.OPT_PRECHECK, String.valueOf(isPrecheck));
		/* build includes & excludes params */
		agentRunner.addAgentParam(AgentParams.OPT_INCLUDES,
				StringUtils.join(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR,
						(Object[]) AnalysisScopePreference.getIncludedLibs()));
		agentRunner.addAgentParam(AgentParams.OPT_EXCLUDES,
				StringUtils.join(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR,
						(Object[]) AnalysisScopePreference.getExcludedLibs()));
		agentRunner.addAgentParam(AgentParams.OPT_VARIABLE_LAYER, MicrobatPreference.getVariableValue());
		try {
			/* test stepLimit */
//			agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 3);
			agentRunner.precheck(config);
			PrecheckInfo info = agentRunner.getPrecheckInfo();
			this.setPrecheckInfo(new PreCheckInformation(info.getThreadNum(), info.getStepTotal(), 
					info.isOverLong(), new ArrayList<>(info.getVisitedLocs())));
			if(!info.isOverLong()){
				agentRunner.runWithDumpFileOption(config, generateTraceFilePath());
//				agentRunner.runWithSocket(config);
				Trace trace = agentRunner.getTrace();
				System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
				System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
				System.out.println("finish!");
				
				return trace;
			}
		} catch (SavException e1) {
			e1.printStackTrace();
		}

		
		return null;
		
	}
	
	private String generateTraceFilePath() {
		appPath.getWorkingDirectory();
		//TODO LLT
		return null;
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


}
