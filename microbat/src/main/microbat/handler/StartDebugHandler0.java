package microbat.handler;

import java.io.IOException;
import java.util.List;

import org.apache.bcel.Repository;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.swt.widgets.Display;

import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReader;
import microbat.behavior.BehaviorReporter;
import microbat.codeanalysis.bytecode.BPVariableRetriever;
import microbat.codeanalysis.runtime.ExecutionStatementCollector;
import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.ProgramExecutor;
import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
import sav.common.core.SavException;
import sav.strategies.dto.AppJavaClassPath;

/*
 * StartDebugHandler0 is a backup 
 */
public class StartDebugHandler0 extends AbstractHandler {

	
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
		}
		
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
					ProgramExecutor tcExecutor = new ProgramExecutor();
					/** 0. clear some static common variables **/
					clearOldData();
					Repository.clearCache();
					
					int stepNum = -1;
					boolean isMultiThread = false;
					List<BreakPoint> executingStatements = null;
					List<BreakPoint> executionOrderList = null;
					try{
						monitor.beginTask("approximating efforts", 1);
						
						ExecutionStatementCollector collector = new ExecutionStatementCollector();
						executingStatements = collector.collectBreakPoints(appClassPath, Settings.isRunTest);
						executionOrderList = collector.getExecutionOrderList();
						stepNum = collector.getStepNum();
						
						if (collector.isMultiThread()) {
							isMultiThread = true;
							System.out.println("WARNING: It is multi-thread program, "
									+ "the steps outside the main thread will be missed");
						}
						monitor.worked(1);		
					}
					finally{
						monitor.done();
					}
					
					if(stepNum != -1){
						try{
							monitor.beginTask("Construct Trace Model", stepNum);
							
							/** 1. parse read/written variables**/
							monitor.setTaskName("parse read/written variables");
							
							BPVariableRetriever retriever = new BPVariableRetriever(executingStatements);
							List<BreakPoint> runningStatements = null;
							try {
								runningStatements = retriever.parsingBreakPoints(appClassPath, false);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
							
							monitor.worked(2);
							
							/**
							 * 2. find the variable scope for:
							 * 1) Identifying the same local variable in different trace nodes.
							 * 2) Generating variable ID for local variable.
							 */
//							monitor.setTaskName("parse variable scopes");
//							List<String> classScope = parseScope(runningStatements);
//							parseLocalVariables(classScope, appClassPath);
							
							if(runningStatements == null){
								System.err.println("Cannot find any slice");
								return Status.OK_STATUS;
							}
							
							monitor.worked(1);
							
							/** 3. extract runtime variables*/
							monitor.setTaskName("extract runtime value for variables");
							
							tcExecutor.setConfig(appClassPath);
							try {
								tcExecutor.run(runningStatements, executionOrderList, monitor, stepNum, Settings.isRunTest);
							} catch (SavException | TimeoutException e) {
								e.printStackTrace();
							} 
							
							/** 4. construct dominance and loop-parent relation*/
							monitor.setTaskName("construct dominance and loop-parent relation");
							
							final Trace trace = tcExecutor.getTrace();
//							trace.setMultiThread(isMultiThread);
							trace.constructDomianceRelation();
							trace.constructLoopParentRelation();
							
							monitor.worked(1);
							
//							Activator.getDefault().setCurrentTrace(trace);
							Display.getDefault().asyncExec(new Runnable(){
								
								@Override
								public void run() {
									TraceView traceView = MicroBatViews.getTraceView();
									traceView.setMainTrace(trace);
									traceView.updateData();
								}
								
							});
						}
						finally{
							monitor.done();
						}
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
