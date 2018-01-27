package microbat.evaluation;

import java.io.File;
import java.util.List;

import org.apache.bcel.Repository;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdi.TimeoutException;

import microbat.Activator;
import microbat.codeanalysis.bytecode.BPVariableRetriever;
import microbat.codeanalysis.runtime.ProgramExecutor;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.preference.MicrobatPreference;
import microbat.util.Settings;
import sav.common.core.SavException;
import sav.strategies.dto.AppJavaClassPath;

public class TraceModelConstructor {
	/**
	 * Take care of project name, buggy class name, buggy line number, ...
	 */
	private void setup(){
	}
	
	public Trace constructTraceModel(AppJavaClassPath appClassPath, List<BreakPoint> executingStatements, 
			List<BreakPoint> executionOrderList, int stepNum)
			throws TimeoutException{
		
		setup();
		
		ProgramExecutor tcExecutor = new ProgramExecutor();
		
		/** 1. clear some static common variables **/
		clearOldData();
		Repository.clearCache();
		
		
		/** 2. parse read/written variables**/
		BPVariableRetriever retriever = new BPVariableRetriever(executingStatements);
		List<BreakPoint> runningStatements = null;
		try {
			runningStatements = retriever.parsingBreakPoints(appClassPath, true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		/**
		 * 3. find the variable scope for:
		 * 1) Identifying the same local variable in different trace nodes.
		 * 2) Generating variable ID for local variable.
		 */
//		List<String> classScope = parseScope(runningStatements);
//		parseLocalVariables(classScope, appClassPath);
		
		/** 4. extract runtime variables*/
		tcExecutor.setConfig(appClassPath);
		try {
			tcExecutor.run(runningStatements, executionOrderList,
					new SubProgressMonitor(new NullProgressMonitor(), 0), stepNum, true);
		} catch (SavException e) {
			e.printStackTrace();
		}
		
		/** 5. construct dominance and loop-parent relation*/
		Trace trace = tcExecutor.getTrace();
		trace.constructDomianceRelation();
		trace.constructLoopParentRelation();
		
		return trace;
	}
	
//	private void parseLocalVariables(final List<String> classScope, AppJavaClassPath appPath) {
//		VariableScopeParser vsParser = new VariableScopeParser();
//		vsParser.parseLocalVariableScopes(classScope, appPath);
//		List<LocalVariableScope> lvsList = vsParser.getVariableScopeList();
//		Settings.localVariableScopes.setVariableScopes(lvsList);
//	}
//	
//	private List<String> parseScope(List<BreakPoint> breakpoints) {
//		List<String> classes = new ArrayList<>();
//		for(BreakPoint bp: breakpoints){
//			if(!classes.contains(bp.getDeclaringCompilationUnitName())){
//				classes.add(bp.getDeclaringCompilationUnitName());
//			}
//		}
//		return classes;
//	}
	
	private void clearOldData(){
		Settings.interestedVariables.clear();
		Settings.wrongPathNodeOrder.clear();
//		Settings.localVariableScopes.clear();
		Settings.potentialCorrectPatterns.clear();
	}
	
	private AppJavaClassPath constructClassPaths(){
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject iProject = myWorkspaceRoot.getProject(Settings.projectName);
		String projectPath = iProject.getLocationURI().getPath();
		projectPath = projectPath.substring(1, projectPath.length());
		projectPath = projectPath.replace("/", File.separator);
		
		String binPath = projectPath + File.separator + "bin"; 
		
		AppJavaClassPath appClassPath = new AppJavaClassPath();
		appClassPath.setJavaHome(Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.JAVA7HOME_PATH));
		
		appClassPath.addClasspath(binPath);
		appClassPath.setWorkingDirectory(projectPath);
		
		return appClassPath;
		
	}
}
