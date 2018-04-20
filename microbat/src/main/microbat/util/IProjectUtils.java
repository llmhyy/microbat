package microbat.util;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
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
	
	public static String getTargetFolder(IJavaProject project) {
		String outputFolder = "";
		try {
			for(String seg: project.getOutputLocation().segments()){
				if(!seg.equals(project.getProject().getName())){
					outputFolder += seg + File.separator;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		String outputPath = project.getProject().getLocation().toOSString() + File.separator + outputFolder;
		return outputPath;
	}
}
