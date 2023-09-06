package microbat.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.handler.callbacks.HandlerCallbackManager;

public class StopDebugPilotHandler extends BaseHandler {
	
	protected static final String DIALOG_INFO_TITLE = "DebugPilot Information";
	protected static final String DIALOG_ERROR_TITLE = "DebugPilot Error";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("DebugPilot") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				HandlerCallbackManager.getInstance().runDebugPilotTerminateCallbacks();
				Job.getJobManager().cancel(DebugPilotHandler.JOB_FAMALY_NAME);
				popInformationDialog("DebugPilot debugging process is stopped.", StopDebugPilotHandler.DIALOG_INFO_TITLE);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}
}
