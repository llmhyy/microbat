package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class BaseHandler extends AbstractHandler {
	
	protected void popErrorDialog(final String errorMsg, final String title) {
		Display.getDefault().asyncExec(() -> {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell, title, errorMsg);
		});
	}
	
	protected void popInformationDialog(final String message, final String title) {
		Display.getDefault().asyncExec(() -> {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openInformation(shell, title, message);
		});
	}
	
}
