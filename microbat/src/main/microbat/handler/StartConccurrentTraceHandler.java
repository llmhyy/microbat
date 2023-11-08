package microbat.handler;

import java.io.IOException;
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
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.concurrent.model.ConcurrentTrace;
import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.handler.StartDebugHandler.MethodFinder;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.preference.AnalysisScopePreference;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.ConcurrentTraceView;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.SequentialConcurrentView;
import microbat.views.TraceView;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * Test handler for testing concurrent trace generation
 * @author Gabau
 *
 */
public class StartConccurrentTraceHandler extends AbstractHandler {

	public static String ID = "microbat.command.startConcTrace";
	
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
					Settings.interestedVariables.clear();
					Settings.potentialCorrectPatterns.clear();
					
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
								SequentialConcurrentView sequentialConcurrentView = 
										MicroBatViews.getSequentialConcurrentView();
										
								if (result == null) {
									sequentialConcurrentView.setTraceNodes(null);
									return;
								}
								ConcurrentTrace trace = ConcurrentTrace.fromTimeStampOrder(
										result.getTraceList());
								trace.setAppJavaClassPath(appClassPath);
								List<Trace> traces = result.getTraceList();
								
								sequentialConcurrentView.setTraceNodes(trace);
//								traceView.setTraceList(traces);
								sequentialConcurrentView.updateData();
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


}

