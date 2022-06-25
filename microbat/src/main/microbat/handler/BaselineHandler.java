package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.recommendation.UserFeedback;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class BaselineHandler extends AbstractHandler {

	TraceView traceView = null;
	UserFeedback userFeedback = null;
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setup();
				if (traceView == null) {
					System.out.println("traceView is null");
					return null;
				}
				
				Trace trace = traceView.getTrace();
				ProbabilityEncoder encoder = new ProbabilityEncoder(trace);
				encoder.setup();
				encoder.encode();
				TraceNode errorNode = encoder.getMostErroneousNode();
				System.out.println("Error Node: " + errorNode.getOrder());
				Display.getDefault().asyncExec(new Runnable(){
					@Override
					public void run() {
						traceView.jumpToNode(trace, errorNode.getOrder(), true);
					}
				});
				
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		return null;
	}
	
	private void setup() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				traceView = MicroBatViews.getTraceView();
			}
			
		});
	}
	
	private void updateFeedbackFromView() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();
				UserFeedback feedback = feedbackView.getFeedback();
				userFeedback = feedback;
			}
		});
	}
	

}
