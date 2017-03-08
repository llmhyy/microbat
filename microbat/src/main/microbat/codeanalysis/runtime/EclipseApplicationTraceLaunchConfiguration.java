package microbat.codeanalysis.runtime;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration;

public class EclipseApplicationTraceLaunchConfiguration extends EclipseApplicationLaunchConfiguration{
	
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			fConfigDir = null;
			monitor.beginTask("", 4); //$NON-NLS-1$
			try {
				preLaunchCheck(configuration, launch, new SubProgressMonitor(monitor, 2));
			} catch (CoreException e) {
				if (e.getStatus().getSeverity() == IStatus.CANCEL) {
					monitor.setCanceled(true);
					return;
				}
				throw e;
			}

			VMRunnerConfiguration runnerConfig = new VMRunnerConfiguration(getMainClass(), getClasspath(configuration));
			runnerConfig.setVMArguments(getVMArguments(configuration));
			runnerConfig.setProgramArguments(getProgramArguments(configuration));
			runnerConfig.setWorkingDirectory(getWorkingDirectory(configuration).getAbsolutePath());
			runnerConfig.setEnvironment(getEnvironment(configuration));
			runnerConfig.setVMSpecificAttributesMap(getVMSpecificAttributesMap(configuration));

			monitor.worked(1);

			setDefaultSourceLocator(configuration);
			manageLaunch(launch);
			IVMRunner runner = getVMRunner(configuration, mode);
			if (runner != null)
				runner.run(runnerConfig, launch, monitor);
			else
				monitor.setCanceled(true);
			monitor.done();
		} catch (final CoreException e) {
			monitor.setCanceled(true);
			throw e;
		}
	}
	
	
	
}
