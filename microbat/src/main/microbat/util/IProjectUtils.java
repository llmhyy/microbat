package microbat.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import sav.common.core.SavRtException;

public class IProjectUtils {

	public static String getJavaHome(IJavaProject project) {
		IVMInstall vmInstall;
		try {
			vmInstall = JavaRuntime.getVMInstall(project);
			return vmInstall.getInstallLocation().getAbsolutePath();
		} catch (CoreException e) {
			throw new SavRtException(e);
		}
	}
}
