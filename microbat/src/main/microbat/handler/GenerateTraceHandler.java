package microbat.handler;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.handler.callbacks.HandlerCallbackManager;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.MicroBatViews;
import sav.strategies.dto.AppJavaClassPath;
import wyk.bp.utils.Log;

public class GenerateTraceHandler extends StartDebugHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// Clear the DebugPilot debugging process if there are any
		HandlerCallbackManager.getInstance().runDebugPilotTerminateCallbacks();
		Job.getJobManager().cancel(DebugPilotHandler.JOB_FAMALY_NAME);
		
		// Clear the path view and program output form
		MicroBatViews.getPathView().updateFeedbackPath(null);
		
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof TextSelection textSelection) {
			try {
				final String selectedText = textSelection.getText();
				IMethod selectedMethod = this.searchTargetMethod(selectedText);
				
				// Get the name of class that method belong to
				IType launchClass = selectedMethod.getDeclaringType();
				final String launchClassName =  launchClass.getFullyQualifiedName();
				
				// Get the target project name
				IJavaProject javaProject = launchClass.getJavaProject();
				final String projectName = javaProject.getElementName();
				
				Settings.projectName = projectName;
				Settings.launchClass = launchClassName;
				Settings.testMethod = selectedMethod.getElementName();
				Settings.isRunTest = selectedMethod.isMainMethod();
				
				final AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths(projectName);
				
				if (selectedMethod.isMainMethod()) {
					appClassPath.setLaunchClass(launchClassName);
				} else {
					appClassPath.setOptionalTestClass(launchClassName);
					appClassPath.setOptionalTestMethod(selectedMethod.getElementName());
					appClassPath.setLaunchClass(TestCaseAnalyzer.TEST_RUNNER);
					appClassPath.setTestCodePath(MicroBatUtil.getSourceFolder(launchClassName, projectName));
				}
				
				List<String> srcFolders = MicroBatUtil.getSourceFolders(projectName);
				appClassPath.setSourceCodePath(appClassPath.getTestCodePath());
				for (String srcFolder : srcFolders) {
					if (!srcFolder.equals(appClassPath.getTestCodePath())) {
						appClassPath.getAdditionalSourceFolders().add(srcFolder);
					}
				}
				
				this.generateTrace(appClassPath);
				return null;
				
			} catch (JavaModelException e) {
				throw new RuntimeException(Log.genLogMsg(this.getClass(), "Cause JavaModelException"));
			}
		}
		return null;
	}
	
	protected IMethod searchTargetMethod(final String methodName) throws JavaModelException {
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editorPart instanceof ITextEditor textEditor) {
			IJavaElement javaElement = JavaUI.getEditorInputJavaElement(textEditor.getEditorInput());
			if (javaElement instanceof ICompilationUnit compilationUnit) {
				for (IType type : compilationUnit.getAllTypes()) {
					if (type.isClass()) {
						for (IMethod method : type.getMethods()) {
							if (method.getElementName().equals(methodName)) {
							    return method;
							}
						}
					}
				}
			}
		}
		throw new RuntimeException(Log.genLogMsg(getClass(), "Cannot find target method: " + methodName));
	}

}
