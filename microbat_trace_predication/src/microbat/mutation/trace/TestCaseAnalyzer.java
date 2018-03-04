package microbat.mutation.trace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Repository;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.PreCheckInformation;
import microbat.codeanalysis.runtime.RunningInformation;
import microbat.evaluation.io.IgnoredTestCaseFiles;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.mutation.ControlDominatedMutationVisitor;
import microbat.mutation.mutation.TraceMutationVisitor;
import microbat.mutation.trace.handlers.MutationGenerationHandler;
import microbat.util.BreakpointUtils;
import microbat.util.IResourceUtils;
import microbat.util.JTestUtil;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import mutation.mutator.MutationVisitor;
import mutation.mutator.Mutator;
import sav.common.core.SavException;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.MutationResult;
import sav.strategies.vm.JavaCompiler;
import sav.strategies.vm.VMConfiguration;
import tregression.empiricalstudy.DeadEndRecord;
import tregression.empiricalstudy.DeadEndReporter;
import tregression.empiricalstudy.EmpiricalTrial;
import tregression.empiricalstudy.Regression;
import tregression.empiricalstudy.Simulator;
import tregression.empiricalstudy.TestCase;
import tregression.empiricalstudy.TrialRecorder;
import tregression.empiricalstudy.training.DED;
import tregression.empiricalstudy.training.DeadEndData;
import tregression.empiricalstudy.training.TrainingDataTransfer;
import tregression.io.ExcelReporter;
import tregression.junit.ParsedTrials;
import tregression.model.PairList;
import tregression.model.Trial;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.LCSBasedTraceMatcher;

public class TestCaseAnalyzer {
	
	public static final String TEST_RUNNER = "microbat.evaluation.junit.MicroBatTestRunner";
	private static final String TMP_DIRECTORY = MutationGenerationHandler.TMP_DIRECTORY;
	private static final String SOURCE_FOLDER_KEY = "sourceFolderPath";
	private static final int STEP_LIMIT = 10000;
	private int muTotal = 10;
	
	public TestCaseAnalyzer(){
	}
	
	public void runEvaluation(IPackageFragment pack, ExcelReporter reporter, boolean isLimitTrialNum, 
			IgnoredTestCaseFiles ignoredTestCaseFiles, ParsedTrials parsedTrials, 
			int trialNumPerTestCase, double[] unclearRates, int optionSearchLimit, IProgressMonitor monitor) throws JavaModelException {
		
		for(IJavaElement javaElement: pack.getChildren()){
			if(javaElement instanceof IPackageFragment){
				runEvaluation((IPackageFragment)javaElement, reporter, isLimitTrialNum, 
						ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates, optionSearchLimit, monitor);
			}
			else if(javaElement instanceof ICompilationUnit){
				ICompilationUnit icu = (ICompilationUnit)javaElement;
				CompilationUnit cu = JavaUtil.convertICompilationUnitToASTNode(icu);
				
				List<MethodDeclaration> testingMethods = JTestUtil.findTestingMethod(cu); 
				if(!testingMethods.isEmpty()){
					String className = JavaUtil.getFullNameOfCompilationUnit(cu);
					
					for(MethodDeclaration testingMethod: testingMethods){
						String methodName = testingMethod.getName().getIdentifier();
						try{
							if (monitor.isCanceled()) {
								return;
							}
							runEvaluationForSingleTestCase(className, methodName, reporter, 
									isLimitTrialNum, ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates, 
									optionSearchLimit, monitor);							
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					
				}
			}
			if (monitor.isCanceled()) {
				System.out.println("Process cancel!");
				return;
			}
		}
		
	}
	
	public boolean runEvaluationForSingleTestCase(String junitClassName, String methodName, ExcelReporter reporter,
			boolean isLimitTrialNum, IgnoredTestCaseFiles ignoredTestCaseFiles, ParsedTrials parsedTrials,
			int trialNumPerTestCase, double[] unclearRates, int optionSearchLimit, IProgressMonitor monitor) 
			throws JavaModelException {
		
		AppJavaClassPath testcaseConfig = createProjectClassPath(junitClassName, methodName);
		
		String testCaseName = junitClassName + "#" + methodName;
		
		if(ignoredTestCaseFiles.contains(testCaseName)){
			return false;
		}
		
		InstrumentationExecutor executor = new InstrumentationExecutor(testcaseConfig,
				generateTraceDir(Settings.projectName, testCaseName, null), "fix");
		
		PreCheckInformation precheckInfo = executor.runPrecheck(STEP_LIMIT);
		if(precheckInfo.isPassTest() && !precheckInfo.isOverLong()){
			System.out.println(testCaseName + " is a passed test case");
			
			System.out.println("identifying the possible mutated location for " + testCaseName);
			List<ClassLocation> executingStatements = convertClassLocation(precheckInfo.getVisitedLocations());
			List<ClassLocation> mutationLocs = findMutationLocation(junitClassName, executingStatements, testcaseConfig);
			List<ClassLocation> staticCandidates = findStaticMutationLocation(junitClassName, mutationLocs, testcaseConfig);
			RunningInformation info = executor.execute(precheckInfo);
			if(!info.isExpectedStepsMet()){
				return false;
			}
			
			Trace correctTrace = info.getTrace();
			Regression.fillMissingInfor(correctTrace, testcaseConfig);
			int thisTrialNum = 0;
			if(!mutationLocs.isEmpty() || !staticCandidates.isEmpty()){
				System.out.println("mutating the tested methods of " + testCaseName);
				Map<String, MutationResult> mutations = generateMutationFiles(mutationLocs, staticCandidates);
				System.out.println("mutation done for " + testCaseName);
				
				if(!mutations.keySet().isEmpty()){
					System.out.println("Start executing mutants for  " + testCaseName);
					System.out.println("===========the mutation is start=================");
				}
				else{
					System.out.println("What a pity, no proper mutants generated for " + testCaseName);
				}
				
				stop:
				for(String tobeMutatedClass: mutations.keySet()){
					MutationResult result = mutations.get(tobeMutatedClass);
					
					for(Integer line: result.getMutatedFiles().keySet()){
						List<File> mutatedFileList = result.getMutatedFiles(line);	
						
						for(File mutationFile: mutatedFileList){
							Trial tmpTrial = new Trial();
							tmpTrial.setTestCaseName(testCaseName);
							tmpTrial.setMutatedFile(mutationFile.toString());
							tmpTrial.setMutatedLineNumber(line);
							
							if(parsedTrials.contains(tmpTrial)){
								continue;
							}
							
							if(monitor.isCanceled()){
								return false;
							}
							testcaseConfig.getPreferences().set(SOURCE_FOLDER_KEY, result.getSourceFolder());
							EvaluationInfo evalInfo = runEvaluationForSingleTrial(tobeMutatedClass, mutationFile, 
									testcaseConfig, line, testCaseName, correctTrace, executingStatements,
									reporter, tmpTrial, unclearRates, precheckInfo.getStepNum(), optionSearchLimit);
							
							if(!evalInfo.isLoopEffective && evalInfo.isValid){
								break;
							}
							
							if(evalInfo.isValid && isLimitTrialNum){
								thisTrialNum++;								
								if(thisTrialNum >= trialNumPerTestCase){
									break stop;
								}
							}
						}
					}
				}
				
				System.out.println("===========all mutation is done==================");
			}
			else{
				System.out.println("However, " + testCaseName + " cannot be mutated");
				ignoredTestCaseFiles.addTestCase(testCaseName);
			}
		}
		else{
			System.out.println(testCaseName + " is a failed test case");
			ignoredTestCaseFiles.addTestCase(testCaseName);
			return false;
		}
		
		return false;
	}
	
	private List<ClassLocation> findStaticMutationLocation(String junitClassName, List<ClassLocation> mutationLocs,
			AppJavaClassPath testcaseConfig) {
		Map<String, List<microbat.model.ClassLocation>> class2PointMap = BreakpointUtils.initBrkpsMap(toMicrobatClassLocation(mutationLocs));
		List<microbat.model.ClassLocation> matchingLocations = new ArrayList<>();
		for(String className: class2PointMap.keySet()){
			LineVisitor visitor = new LineVisitor(class2PointMap.get(className));
			ByteCodeParser.parse(className, visitor, testcaseConfig);
			matchingLocations.addAll(visitor.getResult());
		}
		return convertClassLocation(matchingLocations);
	}

	private List<microbat.model.ClassLocation> toMicrobatClassLocation(List<ClassLocation> locs) {
		List<microbat.model.ClassLocation> result = new ArrayList<>(locs.size());
		for (ClassLocation loc : locs) {
			result.add(new microbat.model.ClassLocation(loc.getClassCanonicalName(), loc.getMethodSign(), loc.getLineNo()));
		}
		return result;
	}

	private List<ClassLocation> convertClassLocation(List<microbat.model.ClassLocation> visitedLocations) {
		List<ClassLocation> locs = new ArrayList<>(visitedLocations.size());
		for (microbat.model.ClassLocation loc : visitedLocations) {
			locs.add(new ClassLocation(loc.getClassCanonicalName(), loc.getMethodSign(), loc.getLineNumber()));
		}
		return locs;
	}

	/**
	 * refer to getMuBugId()
	 */
	private String generateTraceDir(String projectName, String testCaseName, String muBugId) {
		String traceFolder;
		if (muBugId == null) {
			traceFolder = sav.common.core.utils.FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), projectName,
					testCaseName);
		} else {
			traceFolder = sav.common.core.utils.FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), projectName,
					testCaseName, muBugId);
		}
		sav.common.core.utils.FileUtils.createFolder(traceFolder);
		return traceFolder;
	}
	
	class EvaluationInfo{
		boolean isLoopEffective;
		boolean isValid;
		/**
		 * for performance
		 */
		Trace correctTrace;
		
		public EvaluationInfo(boolean isValid, Trace correctTrace, boolean isLoopEffective) {
			super();
			this.isValid = isValid;
			this.correctTrace = correctTrace;
			this.isLoopEffective = isLoopEffective;
		}
	}
	
	private EvaluationInfo runEvaluationForSingleTrial(String tobeMutatedClass, File mutationFile,
			AppJavaClassPath testcaseConfig, int line, String testCaseName, Trace correctTrace,
			List<ClassLocation> executingStatements, ExcelReporter reporter, Trial tmpTrial, double[] unclearRates,
			int stepNum, int optionSearchLimit) throws JavaModelException {
		try {
			MutateInfo mutateInfo = getMutatedTrace(tobeMutatedClass, mutationFile, testcaseConfig, line, testCaseName);
			if (mutateInfo == null) {
				return new EvaluationInfo(false, correctTrace, false);
			}
			if(mutateInfo.isTimeOut){
				System.out.println("Timeout, mutated file: " + mutationFile);
				System.out.println("skip Time Out test case: " + testCaseName);
				return new EvaluationInfo(false, correctTrace, false);
			}
			
			boolean isLoopEffective = false;
			Trace killingMutatantTrace = mutateInfo.killingMutateTrace;
			if(killingMutatantTrace != null && killingMutatantTrace.size() > 1){
				if (null == correctTrace) {
					System.out.println("The correct trace of " + stepNum + " steps is to be generated for " + testCaseName);
					
				}
				long start = System.currentTimeMillis();
				LCSBasedTraceMatcher traceMatcher = new LCSBasedTraceMatcher();
				PairList pairList = traceMatcher.matchTraceNodePair(killingMutatantTrace, correctTrace, null); 
				int matchTime = (int) (System.currentTimeMillis() - start);
				ICompilationUnit iunit = JavaUtil.findNonCacheICompilationUnitInProject(tobeMutatedClass);
				String orgFilePath = IResourceUtils.getAbsolutePathOsStr(iunit.getPath());
				String mutationFilePath = mutationFile.getAbsolutePath();
				DiffMatcher diffMatcher = new MuDiffMatcher(testcaseConfig.getPreferences().get(SOURCE_FOLDER_KEY),
						orgFilePath, mutationFilePath);
				diffMatcher.matchCode();

				Simulator simulator = new Simulator();
				simulator.prepare(killingMutatantTrace, correctTrace, pairList, diffMatcher);
				
				List<EmpiricalTrial> trials0 = simulator.detectMutatedBug(killingMutatantTrace, correctTrace, diffMatcher, 0);
					
				
				TrialRecorder recorder;
				String muBugId = MuRegressionUtils.getMuBugId(mutationFilePath);
				try {
					recorder = new TrialRecorder();
					recorder.export(trials0, Settings.projectName, muBugId);
					boolean foundRootCause = false;
					for(EmpiricalTrial trial: trials0){
						TestCase tc = new TestCase(testCaseName, testCaseName);
						trial.setTestcase(tc.testClass + "#" + tc.testMethod);
						trial.setTraceCollectionTime(killingMutatantTrace.getConstructTime() + correctTrace.getConstructTime());
						trial.setTraceMatchTime(matchTime);
						trial.setBuggyTrace(killingMutatantTrace);
						trial.setFixedTrace(correctTrace);
						trial.setPairList(pairList);
						trial.setDiffMatcher(diffMatcher);
						TraceNode rootCause = trial.getRootCauseFinder().retrieveRootCause(pairList, diffMatcher, killingMutatantTrace, correctTrace);
						if (rootCause != null) {
							foundRootCause = true;
						}
						
						String backupJFile = orgFilePath.replace(".java", "_bk.java");
						FileUtils.copyFile(orgFilePath, backupJFile, true);
						try {
							FileUtils.copyFile(mutationFilePath, orgFilePath, true);
							Settings.iCompilationUnitMap.remove(tobeMutatedClass);
							Settings.compilationUnitMap.remove(tobeMutatedClass);
							if(!trial.getDeadEndRecordList().isEmpty()){
								Repository.clearCache();
								DeadEndRecord record = trial.getDeadEndRecordList().get(0);
								DED datas = new TrainingDataTransfer().transfer(record, trial.getBuggyTrace());
								setTestCase(datas, trial.getTestcase());						
									new DeadEndReporter().export(datas.getAllData(), Settings.projectName, muBugId);
							}
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						} finally {
							FileUtils.copyFile(backupJFile, orgFilePath, true);
							Settings.iCompilationUnitMap.remove(tobeMutatedClass);
							Settings.compilationUnitMap.remove(tobeMutatedClass);
						}
					}
					if (foundRootCause) {
						return new EvaluationInfo(true, correctTrace, isLoopEffective);
					} else {
						if (mutateInfo.traceExecFile != null) {
							new File(mutateInfo.traceExecFile).delete();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("test case has exception when generating trace:");
			System.err.println(tmpTrial);
		} 
		
		return new EvaluationInfo(false, correctTrace, false);
	}

	private void setTestCase(DED datas, String tc) {
		datas.getTrueData().testcase = tc;
		for(DeadEndData data: datas.getFalseDatas()){
			data.testcase = tc;
		}
	}

	class TraceFilePair{
		Trace mutatedTrace;
		String mutatedFile;
		
		public TraceFilePair(Trace mutatedTrace, String mutatedFile) {
			super();
			this.mutatedTrace = mutatedTrace;
			this.mutatedFile = mutatedFile;
		}
		
		public Trace getMutatedTrace() {
			return mutatedTrace;
		}
		
		public void setMutatedTrace(Trace mutatedTrace) {
			this.mutatedTrace = mutatedTrace;
		}
		
		public String getMutatedFile() {
			return mutatedFile;
		}
		
		public void setMutatedFile(String mutatedFile) {
			this.mutatedFile = mutatedFile;
		}
		
	}

	class MutateInfo{
		Trace killingMutateTrace = null;
		boolean isTooLong = false;
		boolean isKill = false;
		boolean isTimeOut = false;
		String traceExecFile;
		
		public MutateInfo(Trace killingMutatnt, boolean isTooLong, boolean isKill, boolean isTimeOut) {
			super();
			this.killingMutateTrace = killingMutatnt;
			this.isTooLong = isTooLong;
			this.isKill = isKill;
			this.isTimeOut = isTimeOut;
		}
	}
	
	private MutateInfo generateMutateTrace(AppJavaClassPath testcaseConfig, String testCaseName, String toBeMutatedClass, int mutatedLine, 
			String mutatedFile){
		Trace killingMutantTrace = null;
		boolean isTooLong = false;
		boolean isKill = true;
		boolean isTimeOut = false;
		String traceExec = null;
		try{
			String traceDir = generateTraceDir(Settings.projectName, testCaseName, MuRegressionUtils.getMuBugId(mutatedFile));
			InstrumentationExecutor executor = new InstrumentationExecutor(testcaseConfig,
					traceDir, "bug");
			executor.setTimeout(30000l);
			PreCheckInformation precheck = executor.runPrecheck(STEP_LIMIT);
			isTimeOut = precheck.isTimeout();
			isKill = !precheck.isPassTest() && !precheck.isTimeout(); 
			String testMethod = testcaseConfig.getOptionalTestClass() + "#" + testcaseConfig.getOptionalTestMethod();
			
			if(isKill){
				System.out.println("KILLED: Now generating trace for " + testMethod + " (mutation: " + mutatedFile + ")");
				if(precheck.isOverLong()){
					System.out.println("The trace is over long for " + testMethod + " (mutation: " + mutatedFile + ")");
					killingMutantTrace = null;
					isTooLong = true;
				}
				else{
					System.out.println("A valid trace of " + precheck.getStepNum() + 
							" steps is to be generated for " + testMethod + " (mutation: " + mutatedFile + ")");
					killingMutantTrace = null;
					long t1 = System.currentTimeMillis();
					RunningInformation info = executor.execute(precheck);
					if(info.isExpectedStepsMet()){
						killingMutantTrace = info.getTrace();
						long t2 = System.currentTimeMillis();
						int time = (int) ((t2-t1)/1000);
						killingMutantTrace.setConstructTime(time);
						/* filling up trace */
						MuRegressionUtils.fillMuBkpJavaFilePath(killingMutantTrace, mutatedFile, toBeMutatedClass);
						Regression.fillMissingInfor(killingMutantTrace, testcaseConfig);
						/* store valid mutated file */
						String destinationFile = FileUtils.getFilePath(traceDir, toBeMutatedClass + ".java");
						FileUtils.copyFile(mutatedFile, destinationFile, true);
						if (!new File(destinationFile).exists()) {
							destinationFile = FileUtils.getFilePath(traceDir, toBeMutatedClass
									.substring(toBeMutatedClass.lastIndexOf("." + 1), toBeMutatedClass.length()) + ".java");
							FileUtils.copyFile(mutatedFile, destinationFile, true);
						}
						traceExec = executor.getTraceExecFilePath();
					}
				}
			}
			else{
				System.out.println("FAIL TO KILL: " + testMethod + " (mutation: " + mutatedFile + ")");
			}
			
		}
		catch(TimeoutException e){
			e.printStackTrace();
			isTimeOut = true;
		}
		
		MutateInfo mutateInfo = new MutateInfo(killingMutantTrace, isTooLong, isKill, isTimeOut);
		mutateInfo.traceExecFile = traceExec;
		return mutateInfo;
	}
	
	private MutateInfo getMutatedTrace(String toBeMutatedClass, File mutationFile, AppJavaClassPath testcaseConfig,
			int mutatedLine, String testCaseName)
					throws MalformedURLException, JavaModelException, IOException, NullPointerException, SavException {
	
//		Settings.compilationUnitMap.clear();
//		Settings.iCompilationUnitMap.clear();
		ICompilationUnit iunit = JavaUtil.findNonCacheICompilationUnitInProject(toBeMutatedClass);
		CompilationUnit unit = JavaUtil.convertICompilationUnitToASTNode(iunit);
		Settings.iCompilationUnitMap.put(toBeMutatedClass, iunit);
		Settings.compilationUnitMap.put(toBeMutatedClass, unit);
		
		/* compile mutation file */
		// backup original .class file
		String targetFolder = IResourceUtils.getAbsolutePathOsStr(iunit.getJavaProject().getOutputLocation());
		String orgClassFilePath = ClassUtils.getClassFilePath(targetFolder, toBeMutatedClass);
		String backupClassFilePath = FileUtils.copyFileToFolder(orgClassFilePath, TMP_DIRECTORY, true);
		try {
			JavaCompiler javaCompiler = new JavaCompiler(new VMConfiguration(testcaseConfig));
			javaCompiler.compile(targetFolder, mutationFile);

			/* generate trace */
			MutateInfo mutateInfo = null;
			mutateInfo = generateMutateTrace(testcaseConfig, testCaseName, toBeMutatedClass, mutatedLine, mutationFile.toString());
			return mutateInfo;
		} catch (SavException e) {
			System.out.println("Compilation error");
//			e.printStackTrace();
		} finally {
			/* revert */
			FileUtils.copyFile(backupClassFilePath, orgClassFilePath, true);
		}
		return null;
	}
	
	private List<ClassLocation> findMutationLocation(String junitClassName, List<ClassLocation> executingStatements,
			AppJavaClassPath appPath) {
		List<ClassLocation> locations = new ArrayList<>();
		Set<String> srcClasses = new HashSet<>();
		Set<String> testClasses = new HashSet<>();
		for (ClassLocation point : executingStatements) {
			if (junitClassName.equals(point.getClassCanonicalName())) {
				continue; // ignore junitClass
			}
//			appPath.getSoureCodePath()
			ClassLocation location = new ClassLocation(point.getClassCanonicalName(), null, point.getLineNo());
			locations.add(location);
		}

		return locations;
	}
	
	private Map<String, MutationResult> generateMutationFiles(List<ClassLocation> dynamicCandidates,
			List<ClassLocation> staticCandidates) {
		ClassLocation cl = dynamicCandidates.get(0);
		String cName = cl.getClassCanonicalName();
		String sourceFolderPath = getSourceFolder(cName);
		
		cleanClassInTestPackage(sourceFolderPath, dynamicCandidates);
		cleanClassInTestPackage(sourceFolderPath, staticCandidates);
		
		Mutator mutator = new Mutator(sourceFolderPath, TMP_DIRECTORY, muTotal);
		MutationVisitor visitor = new TraceMutationVisitor();
		Map<String, MutationResult> mutations = mutator.mutate(dynamicCandidates, visitor);
		visitor = new ControlDominatedMutationVisitor();
		Map<String, MutationResult> cdMutations = mutator.mutate(staticCandidates, visitor);
		MutationResult.merge(mutations, cdMutations);
		
		return mutations;
	}

	private String getSourceFolder(String cName) {
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName);
		IPath uri = unit.getResource().getFullPath();
		String sourceFolderPath = IResourceUtils.getAbsolutePathOsStr(uri);
		cName = cName.substring(0, cName.lastIndexOf(".")).replace(".", File.separator);
		sourceFolderPath = sourceFolderPath.substring(0, sourceFolderPath.indexOf(cName) - 1);
//		System.out.println(sourceFolderPath);
//		System.out.println(cName);
		return sourceFolderPath;
	}
	
	private void cleanClassInTestPackage(String sourceFolderPath,
			List<ClassLocation> locationList) {
		Iterator<ClassLocation> iterator = locationList.iterator();
		while(iterator.hasNext()){
			ClassLocation location = iterator.next();
			String className = location.getClassCanonicalName();
			String fileName  = ClassUtils.getJFilePath(sourceFolderPath, className);
			File file = new File(fileName);
			if(!file.exists()){
				iterator.remove();
			}
		}
	}

	private AppJavaClassPath createProjectClassPath(String junitClassName, String methodName){
		AppJavaClassPath classPath = MicroBatUtil.constructClassPaths();
		classPath.setTestCodePath(getSourceFolder(junitClassName));
		List<String> srcFolders = MicroBatUtil.getSourceFolders(Settings.projectName);
		for (String srcFolder : srcFolders) {
			if (!srcFolder.equals(classPath.getTestCodePath())) {
				classPath.setSourceCodePath(srcFolder);
			}
		}
		if (classPath.getSoureCodePath() == null) {
			classPath.setSourceCodePath(classPath.getTestCodePath());
		}
		String userDir = System.getProperty("user.dir");
		String junitDir = userDir + File.separator + "dropins" + File.separator + "junit_lib";
		
		String junitPath = junitDir + File.separator + "junit.jar";
		String hamcrestCorePath = junitDir + File.separator + "org.hamcrest.core.jar";
		String testRunnerPath = junitDir  + File.separator + "testrunner.jar";
		
		classPath.addClasspath(junitPath);
		classPath.addClasspath(hamcrestCorePath);
		classPath.addClasspath(testRunnerPath);
		
		classPath.addClasspath(junitDir);
		
		classPath.setOptionalTestClass(junitClassName);
		classPath.setOptionalTestMethod(methodName);
		
		classPath.setLaunchClass(TEST_RUNNER);
		
		return classPath;
	}
}
