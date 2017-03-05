package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class EndRecordingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Preparing for Debugging ...") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return null;
	}

}
