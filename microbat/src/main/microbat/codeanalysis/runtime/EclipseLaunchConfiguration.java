package microbat.codeanalysis.runtime;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.LaunchConfiguration;

@SuppressWarnings("restriction")
public class EclipseLaunchConfiguration extends LaunchConfiguration{
	public EclipseLaunchConfiguration(String memento) throws CoreException {
		super(memento);
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
    	/* Setup progress monitor
    	 * - Prepare delegate (0)
    	 * - Pre-launch check (1)
    	 * - [Build before launch (7)]					if build
    	 * - [Incremental build before launch (3)]		if build
    	 * - Final launch validation (1)
    	 * - Initialize source locator (1)
    	 * - Launch delegate (10) */
		SubMonitor lmonitor = SubMonitor.convert(monitor, DebugCoreMessages.LaunchConfiguration_9, build ? 23 : 13);
    	try {
			// bug 28245 - force the delegate to load in case it is interested in launch notifications
			Set<String> modes = getModes();
	    	modes.add(mode);
	    	ILaunchConfigurationDelegate delegate = new EclipseApplicationTraceLaunchConfiguration();

			ILaunchConfigurationDelegate2 delegate2 = null;
			if (delegate instanceof ILaunchConfigurationDelegate2) {
				delegate2 = (ILaunchConfigurationDelegate2) delegate;
			}
			// allow the delegate to provide a launch implementation
			ILaunch launch = null;
			if (delegate2 != null) {
				launch = delegate2.getLaunch(this, mode);
			}
			if (launch == null) {
				launch = new Launch(this, mode, null);
			} else {
				// ensure the launch mode is valid
				if (!mode.equals(launch.getLaunchMode())) {
					IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, MessageFormat.format(DebugCoreMessages.LaunchConfiguration_14, new Object[] {
							mode, launch.getLaunchMode() }), null);
					throw new CoreException(status);
				}
			}
			launch.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, Long.toString(System.currentTimeMillis()));
			boolean captureOutput = getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
			if(!captureOutput) {
			    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false"); //$NON-NLS-1$
			} else {
			    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, null);
			}
			launch.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, getLaunchManager().getEncoding(this));
			if (register) {
				getLaunchManager().addLaunch(launch);
			}
		// perform initial pre-launch sanity checks
			lmonitor.subTask(DebugCoreMessages.LaunchConfiguration_8);

			if (delegate2 != null) {
				if (!(delegate2.preLaunchCheck(this, mode, new SubProgressMonitor(lmonitor, 1)))) {
					getLaunchManager().removeLaunch(launch);
					return launch;
				}
			}
			else {
				lmonitor.worked(1); /* No pre-launch-check */
			}
		// perform pre-launch build
			if (build) {
				IProgressMonitor buildMonitor = new SubProgressMonitor(lmonitor, 10, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				buildMonitor.beginTask(DebugCoreMessages.LaunchConfiguration_7, 10);
				buildMonitor.subTask(DebugCoreMessages.LaunchConfiguration_6);
				boolean tempbuild = build;
				if (delegate2 != null) {
					tempbuild = delegate2.buildForLaunch(this, mode, new SubProgressMonitor(buildMonitor, 7));
				}
				if (tempbuild) {
					buildMonitor.subTask(DebugCoreMessages.LaunchConfiguration_5);
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(buildMonitor, 3));
				}
				else {
					buildMonitor.worked(3); /* No incremental build required */
				}
			}
		// final validation
			lmonitor.subTask(DebugCoreMessages.LaunchConfiguration_4);
			if (delegate2 != null) {
				if (!(delegate2.finalLaunchCheck(this, mode, new SubProgressMonitor(lmonitor, 1)))) {
					getLaunchManager().removeLaunch(launch);
					return launch;
				}
			}
			else {
				lmonitor.worked(1); /* No validation */
			}

			try {
				//initialize the source locator
				lmonitor.subTask(DebugCoreMessages.LaunchConfiguration_3);
				initializeSourceLocator(launch);
				lmonitor.worked(1);

				/* Launch the delegate */
				lmonitor.subTask(DebugCoreMessages.LaunchConfiguration_2);
				delegate.launch(this, mode, launch, new SubProgressMonitor(lmonitor, 10));
			} catch (CoreException e) {
				// if there was an exception, and the launch is empty, remove it
				if (!launch.hasChildren()) {
					getLaunchManager().removeLaunch(launch);
				}
				throw e;
			} catch (RuntimeException e) {
				// if there was a runtime exception, and the launch is empty, remove it
				if (!launch.hasChildren()) {
					getLaunchManager().removeLaunch(launch);
				}
				throw e;
			}
			if (lmonitor.isCanceled()) {
				getLaunchManager().removeLaunch(launch);
			}
			return launch;
    	}
    	finally {
			lmonitor.done();
    	}
    }
}
