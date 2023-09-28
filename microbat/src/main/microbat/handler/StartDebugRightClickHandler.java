package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.internal.Workbench;

import microbat.util.Settings;

/**
 * Handler for right click
 * @author Gabau
 *
 */
public class StartDebugRightClickHandler extends AbstractHandler {

	// basic data class for execution context
	class RightClickHandlerExecutionContext {
		private final IProject contextProject;
		private final String entryPath;
		private final String entryMethod;

		private final boolean isJunit = false;
		// doesn't appear to have a method of detecting
		// this though
		public RightClickHandlerExecutionContext(
				IProject contextProject, String entryPath, String entryMethod) {
			this.contextProject = contextProject;
			this.entryPath = entryPath;
			this.entryMethod = entryMethod;
		}
		
		public String getEntryPath() {
			return entryPath;
		}
		
		public String getEntryMethod() {
			return entryMethod;
		}
		
		public IProject getContextProject() {		
			return contextProject;
		}
		
		public void updateSettings() {
			Settings.launchClass = this.entryPath;
			Settings.isRunTest = this.isJunit;
			Settings.projectName = this.getContextProject().getName();
			
		}
	}

	// todo: update to have multiple sub menus where we can run main vs run junit
	// todo: do not use the settings to deal with
	// the method handling -> deal with it programmatically
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IProject currentProject = getCurrentProject();
		System.out.println(currentProject);
		String prevProject = Settings.projectName;
		String prevLaunchClass = Settings.launchClass;
		boolean prevTestSetting = Settings.isRunTest;
		// set the settings to run the selected project
		
		RightClickHandlerExecutionContext ctxt = getContext();
		ctxt.updateSettings();
//		new StartDebugHandler().execute(event);
		
		Settings.projectName = prevProject;
		Settings.launchClass = prevLaunchClass;
		Settings.isRunTest = prevTestSetting;
		
		
		
		return null;
	}
	
	// basic mvp -> run with the main function for the
	// current project
	// do this by modifying microbat config
	// then run to run the specified code
	public RightClickHandlerExecutionContext getContext() {
        ISelectionService selectionService =     
                Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();    

        ISelection selection = selectionService.getSelection();    
        String launchClass = null;
        String entryPath = null;
        IProject project = null;
        if(selection instanceof IStructuredSelection) {    
            Object element = ((IStructuredSelection)selection).getFirstElement();    

            if (element instanceof IResource) {    
                project= ((IResource)element).getProject();    
            } else if (element instanceof PackageFragmentRootContainer) {
            	PackageFragmentRootContainer rootContainer = ((PackageFragmentRootContainer) element);
                IJavaProject jProject = rootContainer.getJavaProject();
               
                project = jProject.getProject();    
            } else if (element instanceof IJavaElement) {    
            	IJavaElement eJavaElement = ((IJavaElement)element);
                IJavaProject jProject= eJavaElement.getJavaProject();
                PackageFragment parent = (PackageFragment) eJavaElement.getParent();
                if (parent == null) {
                	entryPath = eJavaElement.getElementName()
                			.substring(0, eJavaElement.getElementName().length() - 5);
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String s: parent.names) {
                    	stringBuilder.append(s);
                    	stringBuilder.append('.');
                    }
                    stringBuilder.append(eJavaElement.getElementName()
                    		.substring(0, eJavaElement.getElementName().length() - 5));
                    entryPath = stringBuilder.toString();
                }

                
                
                project = jProject.getProject();    
            }    
        }
        return new RightClickHandlerExecutionContext(project, entryPath, null);
	}
	
	
	// only gets the current project -> need to
	// get the method information to perform 
	// run as
	public static IProject getCurrentProject(){    
        ISelectionService selectionService =     
            Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();    

        ISelection selection = selectionService.getSelection();    

        IProject project = null;    
        if(selection instanceof IStructuredSelection) {    
            Object element = ((IStructuredSelection)selection).getFirstElement();    

            if (element instanceof IResource) {    
                project= ((IResource)element).getProject();    
            } else if (element instanceof PackageFragmentRootContainer) {
            	PackageFragmentRootContainer rootContainer = ((PackageFragmentRootContainer) element);
                IJavaProject jProject = rootContainer.getJavaProject();
                
                project = jProject.getProject();    
            } else if (element instanceof IJavaElement) {    
                IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
                project = jProject.getProject();    
            }    
        }     
        return project;    
    }
	
	
}
