package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.debugpilot.DebugPilotInfo;
import microbat.handler.callbacks.HandlerCallbackManager;

public class StopDebugPilotHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("DebugPilot") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				HandlerCallbackManager.getInstance().runDebugPilotTerminateCallbacks();
				Job.getJobManager().cancel(DebugPilotHandler.JOB_FAMALY_NAME);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

}
