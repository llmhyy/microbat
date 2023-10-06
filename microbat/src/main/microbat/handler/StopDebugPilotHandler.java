package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import debugpilot.userlogger.UserBehaviorLogger;
import debugpilot.userlogger.UserBehaviorType;
import microbat.handler.callbacks.HandlerCallbackManager;
import microbat.views.DialogUtil;

public class StopDebugPilotHandler extends AbstractHandler {
	
	protected static final String DIALOG_INFO_TITLE = "DebugPilot Information";
	protected static final String DIALOG_ERROR_TITLE = "DebugPilot Error";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("DebugPilot") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
//				UserBehaviorLogger.logEvent(UserBehaviorType.STOP_DEBUGPILOT);
				HandlerCallbackManager.getInstance().runDebugPilotTerminateCallbacks();
				Job.getJobManager().cancel(DebugPilotHandler.JOB_FAMALY_NAME);
				DialogUtil.popInformationDialog("DebugPilot debugging process is stopped.", StopDebugPilotHandler.DIALOG_INFO_TITLE);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}
}
