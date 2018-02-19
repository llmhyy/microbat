package microbat.instrumentation.trace.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sav.strategies.dto.AppJavaClassPath;

public class FilterChecker implements IFilterChecker {
	private static final IFilterChecker checker = new FilterChecker();
	
	private List<String> appBinFolders;
	private List<String> extLibs;
	private List<String> bootstrapIncludes = new ArrayList<>();
	private Set<String> includes = new HashSet<>();
	
	public void startup(AppJavaClassPath appClasspath) {
		extLibs = new ArrayList<>();
		appBinFolders = new ArrayList<>();
		String workingDir = getPath(appClasspath.getWorkingDirectory());
		for (String cp : appClasspath.getClasspaths()) {
			String path = getPath(cp);
			if (path.contains(workingDir)) {
				if (path.endsWith(".jar") && !path.contains("junit")) {
					extLibs.add(path);
				} else {
					File binFolder = new File(path);
					if (binFolder.exists() && binFolder.isDirectory()) {
						if (!path.endsWith("/")) {
							path = path + "/";
						}
						appBinFolders.add(path);
					}
				}
			}
		}
		
		System.currentTimeMillis();
		addBootstrapIncludes(ArrayList.class.getName());
		addBootstrapIncludes(HashMap.class.getName());
		addBootstrapIncludes(StringBuffer.class.getName());
	}
	
	private String getPath(String cp) {
		String path = cp;
		path = path.replace("\\", "/");
		if(path.startsWith("/")){
			path = path.substring(1, path.length());
		}
		return path;
	}

	private void addBootstrapIncludes(String... classNames) {
		for (String className : classNames) {
			bootstrapIncludes.add(className.replace(".", "/"));
		}
	}
	
	@Override
	public boolean checkTransformable(String classFName, String path, boolean isBootstrap) {
		if (isBootstrap) {
			return bootstrapIncludes.contains(classFName);
		}
		path = getPath(path);
		File file = new File(path);
		if (file.isFile()) {
			for (String extLib : extLibs) {
				if (path.startsWith(extLib)) {
					return true;
				}
			}
		} else {
			for (String binFolder : appBinFolders) {
				if (binFolder.equals(path)) {
					includes.add(classFName);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean checkAppClass(String classFName) {
		return includes.contains(classFName);
	}
	
	@Override
	public boolean checkExclusive(String className, String methodName) {
		String classFName = className.replace(".", "/");
		return !includes.contains(classFName) && !bootstrapIncludes.contains(classFName);
	}
	
	public static void setup(AppJavaClassPath appClasspath) {
		checker.startup(appClasspath);
	}

	public static boolean isExclusive(String className, String methodName) {
		return checker.checkExclusive(className, methodName);
	}
	
	public static boolean isTransformable(String classFName, String path, boolean isBootstrap) {
		return checker.checkTransformable(classFName, path, isBootstrap);
	}
	
	public static boolean isAppClass(String classFName) {
		return checker.checkAppClass(classFName);
	}

}
