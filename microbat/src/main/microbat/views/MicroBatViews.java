package microbat.views;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class MicroBatViews {
	public static final String DEBUG_FEEDBACK = "microbat.view.debugFeedback";
	public static final String TRACE = "microbat.view.trace";
	public static final String COMCURRENT_TRACE = "microbat.view.currentTrace";
	public static final String REASON = "microbat.view.reason";
	
	public static DebugFeedbackView getDebugFeedbackView(){
		DebugFeedbackView view = null;
		try {
			view = (DebugFeedbackView)PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.DEBUG_FEEDBACK);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	public static TraceView getTraceView(){
		TraceView view = null;
		try {
			view = (TraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.TRACE);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	public static MutilThreadTraceView getConcurrentTraceView(){
		MutilThreadTraceView view = null;
		try {
			view = (MutilThreadTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.COMCURRENT_TRACE);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	public static ReasonView getReasonView(){
		ReasonView view = null;
		try {
			view = (ReasonView)PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.REASON);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
}
