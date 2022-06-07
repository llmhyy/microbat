package microbat.handler;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.Activator;
import microbat.autofeedback.AutoDebugSimulator;
import microbat.autofeedback.AutoFeedbackMethods;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.preference.MicrobatPreference;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;


public class AutoDebugHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// Display auto debug information
		AutoFeedbackMethods selectedMethod = this.getMethod();
		System.out.println("Start Auto Debugging: " + selectedMethod.name());
		
		// Get the trace from trace view
		TraceView traceView = MicroBatViews.getTraceView();
		Trace trace = traceView.getTrace();
		if (trace == null) {
			System.out.println("Error: Please run microbat first to ensure there is a trace in trace view.");
			return null;
		}
		
		// Simulate the auto-debug process in new thread
		try {
			Job job = new Job("Preparing for auto debugging...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					AutoDebugSimulator simulator = new AutoDebugSimulator(traceView, selectedMethod);
					simulator.simulateDebugProcess();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		} catch (Exception e) {
			System.out.println("Error: error occur in the auto-debugging thread:");
			e.printStackTrace();
		}
		return null;
	}
	
	private AutoFeedbackMethods getMethod() {
		String selectedMethodName = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.AUTO_FEEDBACK_METHOD);
		AutoFeedbackMethods selectedMethod = AutoFeedbackMethods.valueOf(selectedMethodName);
		return selectedMethod;
	}
	
}
