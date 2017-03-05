package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.util.Settings;

public class StartRecordingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		if(Settings.isForEclipsePlugin){
			Job job = new Job("...") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					
					System.out.println("start recording");
					
					return Status.OK_STATUS;
				}
			};
			
			job.schedule();
			
		}
		
		return null;
	}

}
