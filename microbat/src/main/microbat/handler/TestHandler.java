package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.Trace;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class TestHandler extends AbstractHandler {
	
	TraceView traceView = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		this.setup();
		if (this.traceView != null) {
			Trace trace = this.traceView.getTrace();
			
			ProbabilityEncoder encoder = new ProbabilityEncoder(trace);
			encoder.setup();
			encoder.encode();
		} else {
			System.out.println("TraceView is null");
		}
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

}
