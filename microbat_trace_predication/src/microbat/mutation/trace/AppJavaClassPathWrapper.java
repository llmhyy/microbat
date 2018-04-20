package microbat.mutation.trace;

import java.util.List;

import microbat.model.trace.Trace;
import microbat.mutation.trace.dto.BackupClassFiles;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.SystemPreferences;

public class AppJavaClassPathWrapper extends AppJavaClassPath {
	private AppJavaClassPath appJavaClassPath;

	public AppJavaClassPathWrapper(AppJavaClassPath appJavaClassPath) {
		this.appJavaClassPath = appJavaClassPath;
	}
	
	
	/**
	 * a dirty workaround for wrong loaded class using in RootCauseFinder.
	 */
	public static void wrapAppClassPath(Trace mutationTrace, Trace correctTrace, BackupClassFiles backupClassFiles) {
		mutationTrace.setAppJavaClassPath(new AppJavaClassPathWrapper(mutationTrace.getAppJavaClassPath()) {
			@Override
			public List<String> getClasspaths() {
				backupClassFiles.restoreMutatedClassFile();
				return super.getClasspaths();
			}
		});
		
		correctTrace.setAppJavaClassPath(new AppJavaClassPathWrapper(correctTrace.getAppJavaClassPath()) {
			@Override
			public List<String> getClasspaths() {
				backupClassFiles.restoreOrgClassFile();
				return super.getClasspaths();
			}
		});
	}

	public String getJavaHome() {
		return appJavaClassPath.getJavaHome();
	}

	public void setJavaHome(String javaHome) {
		appJavaClassPath.setJavaHome(javaHome);
	}

	public List<String> getClasspaths() {
		return appJavaClassPath.getClasspaths();
	}

	public void addClasspaths(List<String> paths) {
		appJavaClassPath.addClasspaths(paths);
	}

	public void addClasspath(String path) {
		appJavaClassPath.addClasspath(path);
	}

	public String getClasspathStr() {
		return appJavaClassPath.getClasspathStr();
	}

	public SystemPreferences getPreferences() {
		return appJavaClassPath.getPreferences();
	}

	public String getWorkingDirectory() {
		return appJavaClassPath.getWorkingDirectory();
	}

	public void setWorkingDirectory(String workingDirectory) {
		appJavaClassPath.setWorkingDirectory(workingDirectory);
	}

	public String getOptionalTestClass() {
		return appJavaClassPath.getOptionalTestClass();
	}

	public void setOptionalTestClass(String optionalTestClass) {
		appJavaClassPath.setOptionalTestClass(optionalTestClass);
	}

	public String getOptionalTestMethod() {
		return appJavaClassPath.getOptionalTestMethod();
	}

	public void setOptionalTestMethod(String optionalTestMethod) {
		appJavaClassPath.setOptionalTestMethod(optionalTestMethod);
	}

	public String getLaunchClass() {
		return appJavaClassPath.getLaunchClass();
	}

	public void setLaunchClass(String launchClass) {
		appJavaClassPath.setLaunchClass(launchClass);
	}

	public String getSoureCodePath() {
		return appJavaClassPath.getSoureCodePath();
	}

	public void setSourceCodePath(String soureCodePath) {
		appJavaClassPath.setSourceCodePath(soureCodePath);
	}

	public String getTestCodePath() {
		return appJavaClassPath.getTestCodePath();
	}

	public void setTestCodePath(String testCodePath) {
		appJavaClassPath.setTestCodePath(testCodePath);
	}

	public List<String> getExternalLibPaths() {
		return appJavaClassPath.getExternalLibPaths();
	}

	public void setExternalLibPaths(List<String> externalLibPaths) {
		appJavaClassPath.setExternalLibPaths(externalLibPaths);
	}

	public void addExternalLibPath(String lib) {
		appJavaClassPath.addExternalLibPath(lib);
	}

	public String getAgentLib() {
		return appJavaClassPath.getAgentLib();
	}

	public void setAgentLib(String agentLib) {
		appJavaClassPath.setAgentLib(agentLib);
	}

	public List<String> getAgentBootstrapPathList() {
		return appJavaClassPath.getAgentBootstrapPathList();
	}

	public void setAgentBootstrapPathList(List<String> agentBootstrapPathList) {
		appJavaClassPath.setAgentBootstrapPathList(agentBootstrapPathList);
	}

	public List<String> getAdditionalSourceFolders() {
		return appJavaClassPath.getAdditionalSourceFolders();
	}

	public void setAdditionalSourceFolders(List<String> additionalSourceFolders) {
		appJavaClassPath.setAdditionalSourceFolders(additionalSourceFolders);
	}

	public List<String> getAllSourceFolders() {
		return appJavaClassPath.getAllSourceFolders();
	}
	
	public ClassLoader getClassLoader() {
		return appJavaClassPath.getClassLoader();
	}

	public void setClassLoader(ClassLoader classLoader) {
		appJavaClassPath.setClassLoader(classLoader);
	}
}
