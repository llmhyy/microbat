package microbat.codeanalysis.runtime;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;


import microbat.agent.TraceAgentRunner;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.AgentParams.LogType;
import microbat.instrumentation.filter.CodeRangeEntry;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.preference.DatabasePreference;
import microbat.preference.ExecutionRangePreference;
import microbat.preference.MicrobatPreference;
import microbat.sql.DBSettings;
import microbat.sql.DbService;
import microbat.util.JavaUtil;
import microbat.util.MinimumASTNodeFinder;
import microbat.util.Settings;
import sav.common.core.SavException;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.VMRunner;

public class InstrumentationExecutor {
	public final static String TRACE_DUMP_FILE_SUFFIX = ".exec";
	
	private AppJavaClassPath appPath;
	private PreCheckInformation precheckInfo;
	private String traceExecFilePath;
	private TraceAgentRunner agentRunner;
	private long timeout = VMRunner.NO_TIME_OUT;
	
	private List<String> includeLibs = Collections.emptyList();
	private List<String> excludeLibs = Collections.emptyList();
	
	public InstrumentationExecutor(AppJavaClassPath appPath, String traceDir, String traceName, 
			List<String> includeLibs, List<String> excludeLibs) {
		this(appPath, generateTraceFilePath(traceDir, traceName), includeLibs, excludeLibs);
		agentRunner = createTraceAgentRunner();
	}
	
	public InstrumentationExecutor(AppJavaClassPath appPath, String traceExecFilePath, 
			List<String> includeLibs, List<String> excludeLibs) {
		this.appPath = appPath;
		this.traceExecFilePath = traceExecFilePath;
		this.includeLibs = includeLibs;
		this.excludeLibs = excludeLibs;
		
		agentRunner = createTraceAgentRunner();
	}
	
	private TraceAgentRunner createTraceAgentRunner() {
		
		if(DBSettings.USE_DB.equals("true")) {
			try {
				DbService.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
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
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(
				LogType.printProgress, 
//				LogType.info,
//				LogType.debug, 
				LogType.error));
		agentRunner.addAgentParam(AgentParams.OPT_REQUIRE_METHOD_SPLITTING,
				MicrobatPreference.getValue(MicrobatPreference.REQUIRE_METHOD_SPLITTING));
		agentRunner.addAgentParam(AgentParams.OPT_AVOID_TO_STRING_OF_PROXY_OBJ, true);
		agentRunner.setTimeout(timeout);
		// FIXME Xuezhi [2]
		List<CodeRangeEntry> entries = ExecutionRangePreference.getCodeRangeEntrys();
		agentRunner.addAgentParams(AgentParams.OPT_CODE_RANGE, entries); 
		return agentRunner;
	}
	
	public RunningInformation run() throws StepLimitException {
		try {
//			System.out.println("first precheck..");
//			agentRunner.precheck(null);
//			PrecheckInfo firstPrecheckInfo = agentRunner.getPrecheckInfo();
//			System.out.println(firstPrecheckInfo);
//			System.out.println("second precheck..");
			
			agentRunner.getConfig().setDebug(Settings.isRunWtihDebugMode);
			agentRunner.getConfig().setPort(9000);
			
			System.out.println("precheck..");
			agentRunner.precheck(null);
			PrecheckInfo info = agentRunner.getPrecheckInfo();
			System.out.println(info);
			PreCheckInformation precheckInfomation = new PreCheckInformation(info.getThreadNum(), info.getStepTotal(),
					info.isOverLong(), new ArrayList<>(info.getVisitedLocs()), info.getExceedingLimitMethods(), info.getLoadedClasses());
			precheckInfomation.setPassTest(agentRunner.isTestSuccessful());
//			precheckInfomation.setUndeterministic(firstPrecheckInfo.getStepTotal() != precheckInfomation.getStepTotal());
			this.setPrecheckInfo(precheckInfomation);
			System.out.println("the trace length is: " + precheckInfomation.getStepNum());
			if (precheckInfomation.isUndeterministic()) {
				System.out.println("undeterministic!!");
			} 
			if (info.isOverLong() /*&& !precheckInfomation.isUndeterministic() */) {
				throw new StepLimitException();
			}
			if (!info.getExceedingLimitMethods().isEmpty()) {
				agentRunner.addAgentParams(AgentParams.OPT_OVER_LONG_METHODS, info.getExceedingLimitMethods());
			}
			
//			agentRunner.getConfig().setDebug(Settings.isRunWtihDebugMode);
//			agentRunner.getConfig().setPort(8000);
			
			RunningInformation rInfo = execute(precheckInfomation);
			return rInfo;
		} catch (SavException e1) {
			e1.printStackTrace();
		}
		
		return new RunningInformation("", -1, -1, new ArrayList<Trace>());
	}
	
	public PreCheckInformation runPrecheck(String dumpFile, int stepLimit) {
		try {
			/* test stepLimit */
			agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, stepLimit);
			if (!agentRunner.precheck(dumpFile)) {
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
			long start = System.currentTimeMillis();
//			agentRunner.getConfig().setPort(8888);
			agentRunner.addAgentParam(AgentParams.OPT_EXPECTED_STEP, info.getStepNum());
			agentRunner.run(DatabasePreference.getReader());
			// agentRunner.runWithSocket();
			RunningInfo result = agentRunner.getRunningInfo();
//			System.out.println(result);
			System.out.println("isExpectedStepsMet? " + result.isExpectedStepsMet());
			System.out.println("trace length: " + result.getMainTrace().size());
			System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
			System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
			System.out.println("finish!");
			agentRunner.removeAgentParam(AgentParams.OPT_EXPECTED_STEP);
			
			Trace trace = result.getMainTrace();
			trace.setAppJavaClassPath(appPath);
//			trace.setMultiThread(info.getThreadNum()!=1);
			
			appendMissingInfo(trace, appPath);
			trace.setConstructTime((int) (System.currentTimeMillis() - start));
			
			RunningInformation information = new RunningInformation(result.getProgramMsg(), result.getExpectedSteps(), 
					result.getCollectedSteps(), result.getTraceList());
			
			return information;
		} catch (SavException e1) {
			e1.printStackTrace();
		}

		return null;
	}
	
//	public List<Trace> execute01 (PreCheckInformation info) {
//		try {
//			long start = System.currentTimeMillis();
////			agentRunner.getConfig().setDebug(true);
////			agentRunner.getConfig().setPort(8888);
//			agentRunner.addAgentParam(AgentParams.OPT_EXPECTED_STEP, info.getStepNum());
//			agentRunner.run01(DatabasePreference.getReader());
//			// agentRunner.runWithSocket();
////			System.out.println(result);
//
//			System.out.println("finish!");
//			agentRunner.removeAgentParam(AgentParams.OPT_EXPECTED_STEP);
//			
//			List<Trace> traces = agentRunner.getTraces();
//			for(Trace trace:traces) {
//				trace.setAppJavaClassPath(appPath);			
//				appendMissingInfo(trace, appPath);
//				trace.setConstructTime((int) (System.currentTimeMillis() - start));
//			}		
//			return traces;
//		} catch (SavException e1) {
//			e1.printStackTrace();
//		}
//
//		return null;
//	}

	public static void appendMissingInfo(Trace trace, AppJavaClassPath appPath) {
		Map<String, String> classNameMap = new HashMap<>();
		Map<String, String> pathMap = new HashMap<>();
		
		for(TraceNode node: trace.getExecutionList()){
			BreakPoint point = node.getBreakPoint();
			if(point.getFullJavaFilePath()==null){
				attachFullPathInfo(point, appPath, classNameMap, pathMap);				
			}
			
			if(!node.getInvocationChildren().isEmpty() && 
					node.getReadVariables().isEmpty()) {
				//check AST completeness
				CompilationUnit cu = JavaUtil.findCompilationUnitInProject(
						node.getDeclaringCompilationUnitName(), appPath);
				MinimumASTNodeFinder finder = new MinimumASTNodeFinder(
						node.getLineNumber(), cu);
				cu.accept(finder);
				ASTNode astNode = finder.getMinimumNode();
				
				if(astNode!=null) {
					int start = cu.getLineNumber(astNode.getStartPosition());
					int end = cu.getLineNumber(astNode.getStartPosition()+astNode.getLength());
					
					TraceNode stepOverPrev = node.getStepOverPrevious();
					while(stepOverPrev!=null && 
							start<=stepOverPrev.getLineNumber() &&
							stepOverPrev.getLineNumber()<=end) {
						List<VarValue> readVars = stepOverPrev.getReadVariables();
						for(VarValue readVar: readVars) {
							if(!node.getReadVariables().contains(readVar)) {
								node.getReadVariables().add(readVar);
							}
						}
						stepOverPrev = stepOverPrev.getStepOverPrevious();
					}
				}
				
			}
		}
	}
	
	public static void attachFullPathInfo(BreakPoint point, AppJavaClassPath appClassPath, 
			Map<String, String> classNameMap, Map<String, String> pathMap){
		String relativePath = point.getDeclaringCompilationUnitName().replace(".", File.separator) + ".java";
		List<String> candidateSourceFolders = appClassPath.getAllSourceFolders();
		for(String candidateSourceFolder: candidateSourceFolders){
			String filePath = candidateSourceFolder + File.separator + relativePath;
			if(new File(filePath).exists()){
				point.setFullJavaFilePath(filePath);
			}
		}
		
		//indicate the declaring compilation name is not correct
		if (point.getFullJavaFilePath() == null) {
			String fullPath = pathMap.get(point.getDeclaringCompilationUnitName());
			if (fullPath != null) {
				point.setFullJavaFilePath(fullPath);
			}
		}
		if(point.getFullJavaFilePath()==null){
			String canonicalClassName = point.getClassCanonicalName(); 
			String declaringCompilationUnitName = classNameMap.get(canonicalClassName);
			String fullPath = pathMap.get(canonicalClassName);
			
			if(declaringCompilationUnitName==null){
				String packageName = point.getPackageName();
				String packageRelativePath = packageName.replace(".", File.separator);
				for(String candidateSourceFolder: candidateSourceFolders){
					String packageFullPath = candidateSourceFolder + File.separator + packageRelativePath;
					declaringCompilationUnitName = findDeclaringCompilationUnitName(packageFullPath, canonicalClassName);
					if(declaringCompilationUnitName!=null){
						fullPath = candidateSourceFolder + File.separator + 
								declaringCompilationUnitName.replace(".", File.separator) + ".java";
						break;
					}
				}
			}
			
			classNameMap.put(canonicalClassName, declaringCompilationUnitName);
			pathMap.put(canonicalClassName, fullPath);
			
			point.setDeclaringCompilationUnitName(declaringCompilationUnitName);
			point.setFullJavaFilePath(fullPath);
			
			if(fullPath==null){
				System.err.println("cannot find the source code file for " + point);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static String findDeclaringCompilationUnitName(String packagePath, String canonicalClassName) {
		File packageFolder = new File(packagePath);
		
		if(!packageFolder.exists()){
			return null;
		}
		
		Collection javaFiles = FileUtils.listFiles(packageFolder, new String[]{"java"}, false);;
		for(Object javaFileObject: javaFiles){
			String javaFile = ((File)javaFileObject).getAbsolutePath();
			CompilationUnit cu = JavaUtil.parseCompilationUnit(javaFile);
			TypeNameFinder finder = new TypeNameFinder(cu, canonicalClassName);
			cu.accept(finder);
			if(finder.isFind){
				return JavaUtil.getFullNameOfCompilationUnit(cu);
			}
		}
		
		return null;
	}

	static class TypeNameFinder extends ASTVisitor{
		CompilationUnit cu;
		boolean isFind = false;
		String canonicalClassName;

		public TypeNameFinder(CompilationUnit cu, String canonicalClassName) {
			super();
			this.cu = cu;
			this.canonicalClassName = canonicalClassName;
		}
		
		public boolean visit(TypeDeclaration type){
			String simpleName = canonicalClassName;
			if(canonicalClassName.contains(".")){
				simpleName = canonicalClassName.substring(
						canonicalClassName.lastIndexOf(".")+1, canonicalClassName.length());
			}
			if(type.getName().getFullyQualifiedName().equals(simpleName)){
				this.isFind = true;
			}
			
			return false;
		}
		
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
