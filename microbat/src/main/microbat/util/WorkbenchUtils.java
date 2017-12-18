package microbat.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import microbat.Activator;

public class WorkbenchUtils {
	private WorkbenchUtils() {
	}
	
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        if (Display.getCurrent() != null) {
            return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
        }
        // need to call from UI thread
        final IWorkbenchWindow[] window = new IWorkbenchWindow[1];
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                window[0] = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
            }
        });
        return window[0];
    }
}
