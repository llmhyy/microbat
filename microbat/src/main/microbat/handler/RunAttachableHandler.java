package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.codeanalysis.runtime.EclipseLaunchConfiguration;
import microbat.util.Settings;

public class RunAttachableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		if(Settings.isForEclipsePlugin){
			Job job = new Job("...") {
				
				@SuppressWarnings("restriction")
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					
					try {
						String memo = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><launchConfiguration local=\"true\" path=\"learntest\"/>";
						EclipseLaunchConfiguration elc = new EclipseLaunchConfiguration(memo);
						elc.launch("debug", monitor);
					} catch (CoreException e2) {
						e2.printStackTrace();
					}
					
					return Status.OK_STATUS;
				}
			};
			
			job.schedule();
			
		}
		
		return null;
	}

}
