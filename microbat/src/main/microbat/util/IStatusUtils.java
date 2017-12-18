/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import microbat.Activator;

/**
 * @author LLT
 *
 */
public class IStatusUtils {
	public static final IStatus OK_STATUS = status(IStatus.OK, ""); //$NON-NLS-1$
	public static final IStatus CANCEL_STATUS = status(IStatus.CANCEL, ""); //$NON-NLS-1$
	
	public static IStatus error(String msg) {
		return status(IStatus.ERROR, msg);
	}

	public static IStatus warning(String msg) {
		return status(IStatus.WARNING, msg);
	}

	public static IStatus info(String msg) {
		return status(IStatus.INFO, msg);
	}
	
	public static IStatus status(int type, String msg) {
		return new Status(type, Activator.PLUGIN_ID, msg);
	}
	
	public static IStatus exception(Throwable ex, String msg) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, ex);
	}
	
	public static IStatus afterRunning(IProgressMonitor monitor) {
		return monitor.isCanceled() ? CANCEL_STATUS : OK_STATUS;
	}
}
