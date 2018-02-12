package microbat.evaluation.views;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class EvaluationViews {
	public static final String BEFORE_TRACE = "microbat.evalView.beforeTraceView";
	public static final String AFTER_TRACE = "microbat.evalView.afterTraceView";
	
	public static BeforeTraceView getBeforeTraceView(){
		BeforeTraceView view = null;
		try {
			view = (BeforeTraceView)PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getActivePage().showView(BEFORE_TRACE);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	public static AfterTraceView getAfterTraceView(){
		AfterTraceView view = null;
		try {
			view = (AfterTraceView)PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getActivePage().showView(AFTER_TRACE);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return view;
	}
}
