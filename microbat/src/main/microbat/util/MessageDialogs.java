/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author LLT
 *
 */
public class MessageDialogs {
	private static final String MSG_DIALOG_TITLE = "Microbat";
	
	public static boolean confirm(Shell shell, String msg) {
		MessageDialog dialog = new MessageDialog(shell, 
				MSG_DIALOG_TITLE, null, msg, MessageDialog.CONFIRM, 
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 
				0);
		return dialog.open() == 0;
	}
	
	public static boolean warningConfirm(Shell shell, String msg) {
		MessageDialog dialog = new MessageDialog(shell, 
				MSG_DIALOG_TITLE, null, msg, MessageDialog.WARNING, 
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 
				0);
		return dialog.open() == 0;
	}
	
	public static void error(Shell shell, String msg) {
		MessageDialog.openError(shell, MSG_DIALOG_TITLE, msg);
	}
	
	public static void warn(Shell shell, String msg) {
		MessageDialog.openWarning(shell, MSG_DIALOG_TITLE, msg);
	}
	
	public static void showErrorInUI(final String msg) { 
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialogs.error(WorkbenchUtils.getActiveWorkbenchWindow()
						.getShell(), msg);
			}
		});
	}
	
	public static void showWarningInUI(final String msg) { 
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialogs.warn(WorkbenchUtils.getActiveWorkbenchWindow()
						.getShell(), msg);
			}
		});
	}
}
