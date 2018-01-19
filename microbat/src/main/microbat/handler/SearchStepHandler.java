package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import microbat.model.trace.Trace;
import microbat.util.Settings;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

@SuppressWarnings("restriction")
public class SearchStepHandler extends AbstractHandler {

	protected boolean directionDown;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		

		if (event.getApplicationContext() instanceof ExpressionContext) {
			ExpressionContext context = (ExpressionContext) event.getApplicationContext();
			IEditorPart editor = (IEditorPart) context.getVariable("activeEditor");

			IVerticalRulerInfo verticalRulerInfo = (IVerticalRulerInfo) editor.getAdapter(IVerticalRulerInfo.class);
			if (verticalRulerInfo != null) {
				
				IEditorInput input = editor.getEditorInput();
				if(input instanceof FileEditorInput){
					
					String className = retrieveClassName(input);
					int lineNumber = verticalRulerInfo.getLineOfLastMouseButtonActivity() + 1;
					
					String expression = Trace.combineTraceNodeExpression(className, lineNumber);
					
					TraceView traceView = MicroBatViews.getTraceView();
					traceView.setSearchText(expression);
					traceView.jumpToNode(expression, this.directionDown);
					
				}
				
			}
		}

		System.currentTimeMillis();
		return null;
	}

	private String retrieveClassName(IEditorInput input) {
		FileEditorInput fInput = (FileEditorInput)input;
		IFile file = fInput.getFile();
		String path = file.getLocationURI().getPath();
		
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject iProject = myWorkspaceRoot.getProject(Settings.projectName);
		String projectPath = iProject.getLocationURI().getPath();
		
		IJavaProject javaProject = JavaCore.create(iProject);
		try {
			for(IPackageFragmentRoot root: javaProject.getAllPackageFragmentRoots()){
				if(root instanceof PackageFragmentRoot){
					String rootName = root.getPath().toPortableString();
					rootName = rootName.substring(javaProject.getElementName().length()+2, rootName.length());
					String prefix = projectPath + "/" + rootName + "/";
					
					if(path.contains(prefix)){
						String fileString = path.substring(prefix.length(), path.length());
						fileString = fileString.substring(0, fileString.length()-5);
						fileString = fileString.replace("/", ".");
						
						String className = fileString;
						return className;
					}
					
				}
			}
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
