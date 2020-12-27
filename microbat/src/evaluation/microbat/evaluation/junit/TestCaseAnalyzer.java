package microbat.evaluation.junit;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Display;

import microbat.evaluation.SimulatedMicroBat;
import microbat.evaluation.TraceModelConstructor;
import microbat.evaluation.io.ExcelReporter;
import microbat.evaluation.io.IgnoredTestCaseFiles;
import microbat.evaluation.model.PairList;
import microbat.evaluation.model.Trial;
import microbat.evaluation.views.AfterTraceView;
import microbat.evaluation.views.BeforeTraceView;
import microbat.evaluation.views.EvaluationViews;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.util.JTestUtil;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import mutation.mutator.MutationUtils;
import mutation.mutator.Mutator;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.MutationResult;

public class TestCaseAnalyzer {
	
	public static final String TEST_RUNNER = "microbat.evaluation.junit.MicroBatTestRunner";
	private static final String TMP_DIRECTORY = "C:\\microbat_evaluation\\";
	
	public TestCaseAnalyzer(){
	}
	
	public void test(){
		String str = "C:\\Users\\YUNLIN~1\\AppData\\Local\\Temp\\mutatedSource8245811234241496344\\47_25_1\\Main.java";
		File file = new File(str);
		
		try {
			String content = FileUtils.readFileToString(file);
			
			ICompilationUnit unit = JavaUtil.findICompilationUnitInProject("com.Main");
			unit.getBuffer().setContents(content);
			unit.save(new NullProgressMonitor(), true);
			
			IProject project = JavaUtil.getSpecificJavaProjectInWorkspace();
			try {
				project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
			System.currentTimeMillis();
			
		} catch (IOException | JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, MutationResult> generateMutationFiles(List<ClassLocation> locationList){
		ClassLocation cl = locationList.get(0);
		String cName = cl.getClassCanonicalName();
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName);
		URI uri = unit.getResource().getLocationURI();
		String sourceFolderPath = uri.toString();
		cName = cName.replace(".", "/") + ".java";
		
		sourceFolderPath = sourceFolderPath.substring(0, sourceFolderPath.indexOf(cName));
		sourceFolderPath = sourceFolderPath.substring(5, sourceFolderPath.length());
		
		cleanClassInTestPackage(sourceFolderPath, locationList);
		System.currentTimeMillis();
		
		Mutator mutator = new Mutator(sourceFolderPath,
				MutationUtils.getMutationFolderPathPrjRelevant(sourceFolderPath, TMP_DIRECTORY));
		Map<String, MutationResult> mutations = mutator.mutate(locationList);
		
		return mutations;
	}
	
	private void cleanClassInTestPackage(String sourceFolderPath,
			List<ClassLocation> locationList) {
		Iterator<ClassLocation> iterator = locationList.iterator();
		while(iterator.hasNext()){
			ClassLocation location = iterator.next();
			String className = location.getClassCanonicalName();
			String fileName  = sourceFolderPath + className.replace(".", "/") + ".java";
			
			File file = new File(fileName);
			if(!file.exists()){
				iterator.remove();
			}
		}
	}


	private static Trace cachedMutatedTrace;
	private static Trace cachedCorrectTrace;
	
	public void runEvaluationForSingleTrial(String testClassName, String testMethodName, String mutationFile, 
			double unclearRate, boolean enableLoopInference, boolean isReuseTrace, int optionSearchLimit) 
					throws JavaModelException, MalformedURLException, IOException {
		
		String testcaseName = testClassName + "#" + testMethodName;
		AppJavaClassPath testcaseConfig = createProjectClassPath(testClassName, testMethodName);
		
		File mutatedFile = new File(mutationFile);
		
		String[] sections = mutationFile.split("\\\\");
		String mutatedLineString = sections[sections.length-2];
		String[] lines = mutatedLineString.split("_");
		int mutatedLine = Integer.valueOf(lines[1]);
		
		CompilationUnit cu = JavaUtil.parseCompilationUnit(mutationFile);
		String mutatedClassName = JavaUtil.getFullNameOfCompilationUnit(cu);
		
		Trace correctTrace = null;
		Trace killingMutatantTrace = null;
		
		if((cachedMutatedTrace==null || cachedCorrectTrace==null) || !isReuseTrace){
			MutateInfo info =
					mutateCode(mutatedClassName, mutatedFile, testcaseConfig, mutatedLine, testcaseName);
			
			if(info.isTooLong){
				System.out.println("mutated trace is over long");
				return;
			}
			
			killingMutatantTrace = info.killingMutateTrace;
			TestCaseRunner checker = new TestCaseRunner();
			
			List<BreakPoint> executingStatements = checker.collectBreakPoints(testcaseConfig, true);
			List<BreakPoint> executionOrderList = checker.getExecutionOrderList();
			if(checker.isOverLong()){
				return;
			}
			
			System.out.println("The correct consists of " + checker.getStepNum() + " steps");
			correctTrace = new TraceModelConstructor().
					constructTraceModel(testcaseConfig, executingStatements, executionOrderList, checker.getStepNum());
			
			cachedCorrectTrace = correctTrace;
			cachedMutatedTrace = killingMutatantTrace;
		}
		else{
			correctTrace = cachedCorrectTrace;
			killingMutatantTrace = cachedMutatedTrace;
		}
		
		SimulatedMicroBat microbat = new SimulatedMicroBat();
		ClassLocation mutatedLocation = new ClassLocation(mutatedClassName, null, mutatedLine);
		microbat.prepare(killingMutatantTrace, correctTrace, mutatedLocation, testcaseName, mutationFile);
		Trial trial;
		try {
			trial = microbat.detectMutatedBug(killingMutatantTrace, correctTrace, mutatedLocation, 
					testcaseName, mutatedFile.toString(), unclearRate, enableLoopInference, optionSearchLimit);	
			if(trial != null){
				System.out.println("Jump " + trial.getJumpSteps().size() + " steps in total");
				if(!trial.isBugFound()){
					System.err.println("Cannot find bug in Mutated File: " + mutatedFile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Mutated File: " + mutatedFile);
		}
		
		visualize(killingMutatantTrace, correctTrace, microbat.getPairList());
	}

	private void visualize(final Trace killingMutatantTrace, final Trace correctTrace, final PairList pairList) {
		
		Display.getDefault().asyncExec(new Runnable(){
			
			@Override
			public void run() {
				BeforeTraceView beforeView = EvaluationViews.getBeforeTraceView();
				beforeView.setMainTrace(correctTrace);
				beforeView.updateData();
				beforeView.setPairList(pairList);
				
				AfterTraceView afterView = EvaluationViews.getAfterTraceView();
				afterView.setMainTrace(killingMutatantTrace);
				afterView.updateData();
				afterView.setPairList(pairList);
			}
			
		});
		
		
		
		
		
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
		}
		
	}
	
	public boolean runEvaluationForSingleTestCase(String className, String methodName, ExcelReporter reporter,
			boolean isLimitTrialNum, IgnoredTestCaseFiles ignoredTestCaseFiles, ParsedTrials parsedTrials,
			int trialNumPerTestCase, double[] unclearRates, int optionSearchLimit, IProgressMonitor monitor) 
			throws JavaModelException {
		
		AppJavaClassPath testcaseConfig = createProjectClassPath(className, methodName);
		String testCaseName = className + "#" + methodName;
		
		if(ignoredTestCaseFiles.contains(testCaseName)){
			return false;
		}
		
		TestCaseRunner checker = new TestCaseRunner();
		checker.checkValidity(testcaseConfig);
		
		Trace correctTrace = null;
		if(checker.isPassingTest()){
			System.out.println(testCaseName + " is a passed test case");
			List<BreakPoint> executingStatements = checker.collectBreakPoints(testcaseConfig, true);
			List<BreakPoint> executionOrderList = checker.getExecutionOrderList();
			if(checker.isOverLong()){
				return false;
			}
			
			System.out.println("identifying the possible mutated location for " + testCaseName);
			List<ClassLocation> locationList = findMutationLocation(executingStatements, testcaseConfig);
			
			int thisTrialNum = 0;
			if(!locationList.isEmpty()){
				System.out.println("mutating the tested methods of " + testCaseName);
				Map<String, MutationResult> mutations = generateMutationFiles(locationList);
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
							
							EvaluationInfo evalInfo = runEvaluationForSingleTrial(tobeMutatedClass, mutationFile, 
									testcaseConfig, line, testCaseName, correctTrace, executingStatements, executionOrderList,
									reporter, tmpTrial, unclearRates, checker.getStepNum(), optionSearchLimit);
							correctTrace = evalInfo.correctTrace;
							
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
	
	private EvaluationInfo runEvaluationForSingleTrial(String tobeMutatedClass, File mutationFile, AppJavaClassPath testcaseConfig, 
			int line, String testCaseName, Trace correctTrace, List<BreakPoint> executingStatements, List<BreakPoint> executionOrderList,
			ExcelReporter reporter, Trial tmpTrial, double[] unclearRates, int stepNum, int optionSearchLimit) throws JavaModelException {
		try {
			MutateInfo mutateInfo = 
					mutateCode(tobeMutatedClass, mutationFile, testcaseConfig, line, testCaseName);
			
			if(mutateInfo == null){
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
				if(null == correctTrace){
					System.out.println("The correct trace of " + stepNum + " steps is to be generated for " + testCaseName);
					correctTrace = new TraceModelConstructor().
							constructTraceModel(testcaseConfig, executingStatements, executionOrderList, stepNum);
				}
				
				ClassLocation mutatedLocation = new ClassLocation(tobeMutatedClass, null, line);
				
				SimulatedMicroBat microbat = new SimulatedMicroBat();
				microbat.prepare(killingMutatantTrace, correctTrace, mutatedLocation, testCaseName, mutationFile.toString());
				
				boolean isValid = true;
				List<Trial> trialList = new ArrayList<>();
				for(int i=0; i<unclearRates.length; i++){
					Trial nonloopTrial = microbat.detectMutatedBug(killingMutatantTrace, correctTrace, 
							mutatedLocation, testCaseName, mutationFile.toString(), unclearRates[i], false, optionSearchLimit);
					Trial loopTrial = microbat.detectMutatedBug(killingMutatantTrace, correctTrace, 
							mutatedLocation, testCaseName, mutationFile.toString(), unclearRates[i], true, optionSearchLimit);
					
					if(loopTrial==null || nonloopTrial==null){
						isValid = false;
						break;
					}
					
					nonloopTrial.setTime(killingMutatantTrace.getConstructTime());
					loopTrial.setTime(killingMutatantTrace.getConstructTime());
					trialList.add(nonloopTrial);
					trialList.add(loopTrial);
					
					if(i==0 && loopTrial.getJumpSteps().size()<nonloopTrial.getJumpSteps().size()){
						isLoopEffective = true;
						System.out.println("GOOD NEWS: loop inference takes effect!");
					}
				}
				
				if(isValid){
					reporter.export(trialList);
					return new EvaluationInfo(true, correctTrace, isLoopEffective);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("test case has exception when generating trace:");
			System.err.println(tmpTrial);
		} 
		
		return new EvaluationInfo(false, correctTrace, false);
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
		
		public MutateInfo(Trace killingMutatnt, boolean isTooLong, boolean isKill, boolean isTimeOut) {
			super();
			this.killingMutateTrace = killingMutatnt;
			this.isTooLong = isTooLong;
			this.isKill = isKill;
			this.isTimeOut = isTimeOut;
		}
		
		
	}
	
	private MutateInfo generateMutateTrace(AppJavaClassPath testcaseConfig, ICompilationUnit iunit, int mutatedLine, 
			String mutatedFile){
		Trace killingMutantTrace = null;
		boolean isTooLong = false;
		boolean isKill = true;
		boolean isTimeOut = false;
		try{
			TestCaseRunner checker = new TestCaseRunner();
			checker.checkValidity(testcaseConfig);
			
			isKill = !checker.isPassingTest() && !checker.hasCompilationError();
			String testMethod = testcaseConfig.getOptionalTestClass() + "#" + testcaseConfig.getOptionalTestMethod();
			
			if(isKill){
				System.out.println("KILLED: Now generating trace for " + testMethod + " (mutation: " + mutatedFile + ")");
				TraceModelConstructor constructor = new TraceModelConstructor();
				
				List<BreakPoint> executingStatements = checker.collectBreakPoints(testcaseConfig, true);
				List<BreakPoint> executionOrderList = checker.getExecutionOrderList();
				
				if(checker.isOverLong()){
					System.out.println("The trace is over long for " + testMethod + " (mutation: " + mutatedFile + ")");
					killingMutantTrace = null;
					isTooLong = true;
				}
				else{
					System.out.println("A valid trace of " + checker.getStepNum() + 
							" steps is to be generated for " + testMethod + " (mutation: " + mutatedFile + ")");
					killingMutantTrace = null;
					long t1 = System.currentTimeMillis();
					killingMutantTrace = constructor.constructTraceModel(testcaseConfig, executingStatements, 
							executionOrderList, checker.getStepNum());
					long t2 = System.currentTimeMillis();
					int time = (int) ((t2-t1)/1000);
					killingMutantTrace.setConstructTime(time);
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
		return mutateInfo;
	}
	
	private MutateInfo mutateCode(String toBeMutatedClass, File mutationFile, AppJavaClassPath testcaseConfig, 
			int mutatedLine, String testCaseName) 
			throws MalformedURLException, JavaModelException, IOException, NullPointerException {
		
//		Settings.compilationUnitMap.clear();
//		Settings.iCompilationUnitMap.clear();
		
		ICompilationUnit iunit = JavaUtil.findNonCacheICompilationUnitInProject(toBeMutatedClass);
		CompilationUnit unit = JavaUtil.convertICompilationUnitToASTNode(iunit);
		Settings.iCompilationUnitMap.put(toBeMutatedClass, iunit);
		Settings.compilationUnitMap.put(toBeMutatedClass, unit);
		
		String originalCodeText = iunit.getSource();
		
		String mutatedCodeText = FileUtils.readFileToString(mutationFile);
		
		iunit.getBuffer().setContents(mutatedCodeText);
		iunit.save(new NullProgressMonitor(), true);
		autoCompile();
		
		MutateInfo mutateInfo = null;
		try{
			mutateInfo = generateMutateTrace(testcaseConfig, iunit, mutatedLine, mutationFile.toString());			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		iunit.getBuffer().setContents(originalCodeText);
		iunit.save(new NullProgressMonitor(), true);
		autoCompile();
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		iunit = JavaUtil.findNonCacheICompilationUnitInProject(toBeMutatedClass);
		unit = JavaUtil.convertICompilationUnitToASTNode(iunit);
		Settings.iCompilationUnitMap.put(toBeMutatedClass, iunit);
		Settings.compilationUnitMap.put(toBeMutatedClass, unit);
		
//		Settings.compilationUnitMap.clear();
//		Settings.iCompilationUnitMap.clear();
		
		return mutateInfo;
	}
	
	private void autoCompile() {
		IProject project = JavaUtil.getSpecificJavaProjectInWorkspace();
		try {
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private List<ClassLocation> findMutationLocation(List<BreakPoint> executingStatements, AppJavaClassPath appPath) {
		List<ClassLocation> locations = new ArrayList<>();
		
//		for(BreakPoint point: executingStatements){
//			ClassLocation location = new ClassLocation(point.getDeclaringCompilationUnitName(), 
//					null, point.getLineNumber());
//			locations.add(location);	
//		}
		
		for(BreakPoint point: executingStatements){
			String className = point.getDeclaringCompilationUnitName();
			CompilationUnit cu = JavaUtil.findCompilationUnitInProject(className, appPath);
			if(cu != null && !JTestUtil.isInTestCase(className, appPath)){
				MutationPointChecker checker = new MutationPointChecker(cu, point.getLineNumber());
				cu.accept(checker);
				
				if(checker.isLoopInsider()){
					ClassLocation location = new ClassLocation(className, 
							null, point.getLineNumber());
					locations.add(location);					
				}
			}
		}
		
		List<ClassLocation> invocationMutationPoints = findInvocationMutations(executingStatements, appPath);
		for(ClassLocation location: invocationMutationPoints){
			if(!locations.contains(location)){
				locations.add(location);
			}
		}
		
		return locations;
	}
	
	private List<ClassLocation> findInvocationMutations(final List<BreakPoint> executingStatements, final AppJavaClassPath appPath) {
		final List<ClassLocation> validLocationList = new ArrayList<>();
		
		for(final BreakPoint point: executingStatements){
			final CompilationUnit cu = JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName(), appPath);
			cu.accept(new ASTVisitor() {
				public boolean visit(MethodInvocation invocation){
					int startLine = cu.getLineNumber(invocation.getStartPosition());
					
					
					if(startLine == point.getLineNumber()){
						if(startLine == 73){
							System.currentTimeMillis();
						}
						
						/**
						 * check whether this invocation is above loop
						 */
						ASTNode parent = invocation.getParent();
						while(parent!=null && !(parent instanceof Block)){
							parent = parent.getParent();
						}
						if(parent==null){
							return false;
						}
						
						LoopStatementChecker checker = new LoopStatementChecker(invocation, cu);
						parent.accept(checker);
						
						if(checker.isValid){
							IMethodBinding invocationBinding = invocation.resolveMethodBinding();
							IMethodBinding declarationBinding = invocationBinding.getMethodDeclaration();
							
							ITypeBinding type = declarationBinding.getDeclaringClass();
							CompilationUnit cuOfDec = JavaUtil.findCompilationUnitInProject(type.getQualifiedName(), appPath);
							
							if(cuOfDec == null){
								return false;
							}
							
							MethodDeclaration declaration = (MethodDeclaration) cuOfDec.findDeclaringNode(declarationBinding);
							if(declaration == null){
								return false;
							}
							
							int startMDLine = cuOfDec.getLineNumber(declaration.getStartPosition());
							int endMDLine = cuOfDec.getLineNumber(declaration.getStartPosition()+declaration.getLength());
							
							for(BreakPoint point: executingStatements){
								if(startMDLine<point.getLineNumber() && point.getLineNumber()<endMDLine){
									ClassLocation location = 
											new ClassLocation(point.getDeclaringCompilationUnitName(), null, point.getLineNumber());
									if(!validLocationList.contains(location)){
										validLocationList.add(location);
									}
								}
							}
						}
					}
					
					return false;
				}
			});
		}
		
		return validLocationList;
	}

	private AppJavaClassPath createProjectClassPath(String className, String methodName){
		AppJavaClassPath classPath = MicroBatUtil.constructClassPaths();
		
		String userDir = System.getProperty("user.dir");
		String junitDir = userDir + File.separator + "dropins" + File.separator + "junit_lib";
		
		String junitPath = junitDir + File.separator + "junit.jar";
		String hamcrestCorePath = junitDir + File.separator + "org.hamcrest.core.jar";
		String testRunnerPath = junitDir  + File.separator + "testrunner.jar";
		
		classPath.addClasspath(junitPath);
		classPath.addClasspath(hamcrestCorePath);
		classPath.addClasspath(testRunnerPath);
		
		classPath.addClasspath(junitDir);
		
		classPath.setOptionalTestClass(className);
		classPath.setOptionalTestMethod(methodName);
		
		classPath.setLaunchClass(TEST_RUNNER);
		
		return classPath;
	}
}
