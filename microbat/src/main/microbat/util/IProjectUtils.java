package microbat.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
	
	
	public static ClassLoader getPrjClassLoader(IJavaProject javaProject) {
		try {
			List<URL> urlList = new ArrayList<URL>();
			List<String> classPathEntries = getPrjectClasspath(javaProject);
			for (String cpEntry : classPathEntries) {
				IPath path = new Path(cpEntry);
				URL url = path.toFile().toURI().toURL();
				urlList.add(url);
			}
			ClassLoader parentClassLoader = javaProject.getClass().getClassLoader();
			URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
			URLClassLoader classLoader = new URLClassLoader(urls, parentClassLoader);
			return classLoader;
		} catch (MalformedURLException e) {
			throw new SavRtException(e);
		}
	}
	
	public static List<String> getPrjectClasspath(IJavaProject project) {
		try {
			String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(project);
			return Arrays.asList(classPathEntries);
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
	
	public static String getProjectFolder(IProject project) {
		return project.getLocation().toOSString();
	}
}
