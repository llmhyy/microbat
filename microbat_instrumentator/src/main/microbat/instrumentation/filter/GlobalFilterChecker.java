package microbat.instrumentation.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.model.trace.Trace;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;

public class GlobalFilterChecker {
	private static final GlobalFilterChecker checker = new GlobalFilterChecker();
	
	private List<String> appBinFolders;
	private List<String> extLibs;
	private List<String> bootstrapIncludes = new ArrayList<>();
	private Set<String> includes = new HashSet<>();
	private WildcardMatcher extIncludesMatcher; // className
	private WildcardMatcher extExcludesMatcher; // className
	
	private List<String> includedLibraryClasses = new ArrayList<>();
	private List<String> excludedLibraryClasses = new ArrayList<>();
	
	public static GlobalFilterChecker getInstance() {
		return checker;
	}
	
	public void startup(AppJavaClassPath appClasspath, String includeExpression, String excludeExpression) {
		extLibs = new ArrayList<>();
		appBinFolders = new ArrayList<>();
		ExecutionTracer.appJavaClassPath = appClasspath;
		String workingDir = getPath(appClasspath.getWorkingDirectory());
		for (String cp : appClasspath.getClasspaths()) {
			String path = getPath(cp);
			if (path.contains(workingDir)) {
				if (path.endsWith(".jar") && !path.contains("junit")) {
					extLibs.add(path);
				} else { 
					File binFolder = new File(cp);
					if (binFolder.exists() && binFolder.isDirectory()) {
						path = getDir(path);
						appBinFolders.add(path);
					}
				}
			}
		}
		if (!StringUtils.isEmpty(includeExpression)) {
			extIncludesMatcher = new WildcardMatcher(includeExpression);
		}
		if (!StringUtils.isEmpty(excludeExpression)) {
			extExcludesMatcher = new WildcardMatcher(excludeExpression);
		}
	}
	
	private String getDir(String path) {
		if (!path.endsWith("/")) {
			return path + "/";
		}
		return path;
	}

	private String getPath(String cp) {
		String path = cp;
		path = path.replace("\\", "/");
		if(path.startsWith("/")){
			path = path.substring(1, path.length());
		}
		return path;
	}

	@SuppressWarnings("unused")
	private void addBootstrapIncludes(String... classNames) {
		for (String className : classNames) {
			bootstrapIncludes.add(className.replace(".", "/"));
		}
	}

	public boolean checkTransformable(String classFName, String path, boolean isBootstrap) {
		if (!JdkFilter.filter(getClassName(classFName))) {
			logIncludeExtLib(classFName, true, false);
			return false;
		}
		boolean match = false;
		boolean isExtLib = true;
		if (isBootstrap) {
			if (bootstrapIncludes.contains(classFName)) {
				return true;
			}
		} else {
			path = getPath(path);
			File file = new File(path);
			if (file.isFile()) {
				/* exclude extLib by default */
			} else {
				for (String binFolder : appBinFolders) {
					if (binFolder.equals(getDir(path))) {
						match = true;
						isExtLib = false;
						includes.add(classFName);
						break;
					}
				}
			}
		}
		match = matchExtIncludes(classFName, match);
		if (match && isBootstrap) {
			bootstrapIncludes.add(classFName);
		}
		logIncludeExtLib(classFName, isExtLib, match);
		return match;
	}
	
	private void logIncludeExtLib(String classFName, boolean isExtLib, boolean isIncluded) {
		if (isExtLib) {
			String className = getClassName(classFName);
			if (className.startsWith("microbat.")){
				return;
			}
			if (isIncluded) {
				includedLibraryClasses.add(className);
			} else {
				excludedLibraryClasses.add(className);
			}
		}
	}
	
	private boolean matchExtIncludes(String classFName, boolean match) {
		String className = getClassName(classFName);
		if (!match && (extIncludesMatcher != null)) {
			match = extIncludesMatcher.matches(className);
		}
		
		if (extExcludesMatcher != null) {
			match &= !extExcludesMatcher.matches(className);
		}
		return match;
	}

	private String getClassName(String classFName) {
		return classFName.replace("/", ".");
	}

	public boolean checkAppClass(String classFName) {
		return includes.contains(classFName);
	}
	
	public boolean checkExclusive(String className, String methodName) {
		String classFName = className.replace(".", "/");
		return !includes.contains(classFName) /* && !bootstrapIncludes.contains(classFName) */;
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
	
	public static boolean isAppClazz(String className) {
		return checker.checkAppClass(className.replace(".", "/"));
	}

	public static void setup(AppJavaClassPath appPath, String includesExpression, String exludesExpression) {
		checker.startup(appPath, includesExpression, exludesExpression);
	}

	public static void addFilterInfo(Trace trace) {
		if (checker instanceof GlobalFilterChecker) {
			GlobalFilterChecker filterChecker = (GlobalFilterChecker) checker;
			trace.setExcludedLibraryClasses(filterChecker.excludedLibraryClasses);
			trace.setIncludedLibraryClasses(filterChecker.includedLibraryClasses);
		}
	}
	
	public List<String> getBootstrapIncludes() {
		return bootstrapIncludes;
	}
	
	public List<String> getIncludedLibraryClasses() {
		return includedLibraryClasses;
	}
}
