package microbat.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.swt.widgets.Display;

import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReader;
import microbat.behavior.BehaviorReporter;
import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.RunningInformation;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.preference.AnalysisScopePreference;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
//import microbat.views.TraceView;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

public class StartDebugHandler extends AbstractHandler {
	
	private void clearOldData(){
		Settings.interestedVariables.clear();
		Settings.wrongPathNodeOrder.clear();
//		Settings.localVariableScopes.clear();
		Settings.potentialCorrectPatterns.clear();
		Settings.checkingStateStack.clear();
		
		Settings.compilationUnitMap.clear();
		Settings.iCompilationUnitMap.clear();
		
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				DebugFeedbackView view = MicroBatViews.getDebugFeedbackView();
				view.clear();
			}
			
		});
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		if (Settings.isRunTest) {
			appClassPath.setOptionalTestClass(Settings.launchClass);
			appClassPath.setOptionalTestMethod(Settings.testMethod);
			appClassPath.setLaunchClass(TestCaseAnalyzer.TEST_RUNNER);
			appClassPath.setTestCodePath(MicroBatUtil.getSourceFolder(Settings.launchClass, Settings.projectName));
		}
		List<String> srcFolders = MicroBatUtil.getSourceFolders(Settings.projectName);
		appClassPath.setSourceCodePath(appClassPath.getTestCodePath());
		for (String srcFolder : srcFolders) {
			if (!srcFolder.equals(appClassPath.getTestCodePath())) {
				appClassPath.getAdditionalSourceFolders().add(srcFolder);
			}
		}
		
//		InstrumentationExecutor ex = new InstrumentationExecutor(appClassPath);
//		ex.run();
		
		try {
			new BehaviorReader().readXLSX();
		} catch (IOException e) {
			e.printStackTrace();
		};
		
		Behavior behavior = BehaviorData.getOrNewBehavior(Settings.launchClass);
		behavior.increaseGenerateTrace();
		new BehaviorReporter(Settings.launchClass).export(BehaviorData.projectBehavior);
		
		try {
			
			Job job = new Job("Preparing for Debugging ...") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try{
						monitor.beginTask("Construct Trace Model", 100);
						
						List<String> includedClassNames = AnalysisScopePreference.getIncludedLibList();
						List<String> excludedClassNames = AnalysisScopePreference.getExcludedLibList();
						InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
								generateTraceDir(appClassPath), "trace", includedClassNames, excludedClassNames);
						final RunningInfo result = executor.run();
						
						monitor.worked(80);
						
						Display.getDefault().asyncExec(new Runnable(){
							
							@Override
							public void run() {
								TraceView traceView = MicroBatViews.getTraceView();
								if (result == null) {
									traceView.setMainTrace(null);
									traceView.setTraceList(null);
									return;
								}
								Trace trace = result.getMainTrace();
								trace.setAppJavaClassPath(appClassPath);
								List<Trace> traces = result.getTraceList();
								
								traceView.setMainTrace(trace);
								traceView.setTraceList(traces);
								traceView.updateData();
							}
							
						});
					} catch (StepLimitException e) {
						System.out.println("Step limit exceeded");
						e.printStackTrace();
					} catch (Exception e) {
						System.out.println("Debug failed");
						e.printStackTrace();
					}
					finally{
						monitor.done();
					}
					
					return Status.OK_STATUS;
				}

//				private List<String> parseScope(List<BreakPoint> breakpoints) {
//					List<String> classes = new ArrayList<>();
//					for(BreakPoint bp: breakpoints){
//						if(!classes.contains(bp.getDeclaringCompilationUnitName())){
//							classes.add(bp.getDeclaringCompilationUnitName());
//						}
//					}
//					return classes;
//				}
			};
			job.schedule();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	protected String generateTraceDir(AppJavaClassPath appPath) {
		String traceFolder;
		if (appPath.getOptionalTestClass() != null) {
			traceFolder = FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), 
					Settings.projectName,
					appPath.getOptionalTestClass(), 
					appPath.getOptionalTestMethod());
		} else {
			traceFolder = FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), 
					Settings.projectName, 
					appPath.getLaunchClass()); 
		}
		FileUtils.createFolder(traceFolder);
		return traceFolder;
	}

	class MethodFinder extends ASTVisitor{
		CompilationUnit cu;
		MethodDeclaration methodDeclaration;
		int lineNumber;
		
		public MethodFinder(CompilationUnit cu, int lineNumber) {
			super();
			this.cu = cu;
			this.lineNumber = lineNumber;
		}

		public boolean visit(MethodDeclaration md){
			int startLine = cu.getLineNumber(md.getStartPosition());
			int endLine = cu.getLineNumber(md.getStartPosition()+md.getLength());
			
			if(startLine <= lineNumber && lineNumber <= endLine){
				methodDeclaration = md;
			}
			
			return false;
		}
	}
	
	private String convertSignature(String classQulifiedName, int lineNumber, AppJavaClassPath appPath) {
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(classQulifiedName, appPath);
		
		MethodFinder finder = new MethodFinder(cu, lineNumber);
		cu.accept(finder);
		
		MethodDeclaration methodDeclaration = finder.methodDeclaration;
		IMethodBinding mBinding = methodDeclaration.resolveBinding();
		String methodSig = JavaUtil.generateMethodSignature(mBinding);
		
		return methodSig;
	}

	/**
	 * This method is used to build the scope of local variables.
	 * @param classScope
	 */
//	private void parseLocalVariables(final List<String> classScope, AppJavaClassPath appPath) {
//		VariableScopeParser vsParser = new VariableScopeParser();
//		vsParser.parseLocalVariableScopes(classScope, appPath);
//		List<LocalVariableScope> lvsList = vsParser.getVariableScopeList();
////		System.out.println(lvsList);
//		Settings.localVariableScopes.setVariableScopes(lvsList);
//	}
	
	
//	@SuppressWarnings("restriction")
//	private List<String> getSourceLocation(){
//		IProject iProject = JavaUtil.getSpecificJavaProjectInWorkspace();
//		IJavaProject javaProject = JavaCore.create(iProject);
//		
//		List<String> paths = new ArrayList<String>();
//		try {
//			for(IPackageFragmentRoot root: javaProject.getAllPackageFragmentRoots()){
//				if(!(root instanceof JarPackageFragmentRoot)){
//					String path = root.getResource().getLocationURI().getPath();
//					path = path.substring(1, path.length());
//					//path = path.substring(0, path.length()-Settings.projectName.length()-1);
//					path = path.replace("/", "\\");
//					
//					if(!paths.contains(path)){
//						paths.add(path);
//					}					
//				}
//			}
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//		}
//		
//		return paths;
//	}
	
//	private List<BreakPoint> testSlicing(){
//		List<BreakPoint> breakpoints = new ArrayList<BreakPoint>();
//		String clazz = "com.Main";
//	
//		BreakPoint bkp3 = new BreakPoint(clazz, null, 12);
//		bkp3.addVars(new Variable("c"));
//		bkp3.addVars(new Variable("tag", "tag", VarScope.THIS));
//		bkp3.addVars(new Variable("output"));
//		bkp3.addVars(new Variable("i"));
//	
//		BreakPoint bkp2 = new BreakPoint(clazz, null, 14);
//		bkp2.addVars(new Variable("c"));
//		bkp2.addVars(new Variable("tag", "tag", VarScope.THIS));
//		bkp2.addVars(new Variable("output"));
//	
//		BreakPoint bkp1 = new BreakPoint(clazz, null, 17);
//		bkp1.addVars(new Variable("c"));
//		bkp1.addVars(new Variable("tag", "tag", VarScope.THIS));
//		bkp1.addVars(new Variable("output"));
//	
//		breakpoints.add(bkp3);
//		breakpoints.add(bkp2);
//		breakpoints.add(bkp1);
//	
//		return breakpoints;
//	}
}
