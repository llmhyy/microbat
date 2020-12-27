package microbat.views;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import microbat.util.Settings;

public class MicroBatViews {
	public static final String DEBUG_FEEDBACK = "microbat.view.debugFeedback";
	public static final String TRACE = "microbat.view.trace";
	public static final String CONCURRENT_TRACE = "microbat.view.concurrentTrace";
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
			if(Settings.supportConcurrentTrace) {
				view = (ConcurrentTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.CONCURRENT_TRACE);
			}
			else {
				view = (TraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.TRACE);				
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
//	public static ConcurrentTraceView getConcurrentTraceView(){
//		ConcurrentTraceView view = null;
//		try {
//			view = (ConcurrentTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.CONCURRENT_TRACE);
//		} catch (PartInitException e) {
//			e.printStackTrace();
//		}
//		
//		return view;
//	}
	
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
