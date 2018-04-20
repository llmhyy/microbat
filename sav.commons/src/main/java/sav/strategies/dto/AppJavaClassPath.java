/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.StringUtils;

/**
 * @author LLT, modified by Yun Lin.
 * 
 */
public class AppJavaClassPath {
	private String javaHome;
	private String workingDirectory;
	private List<String> classpaths; // classpath order is important!
	
	private List<String> externalLibPaths = new ArrayList<>();
	
	private String launchClass;
	
	private String agentLib;
	
	private List<String> agentBootstrapPathList = new ArrayList<>();
	
	/**
	 * If Microbat is running a test case (JUnit), user need to specify which test case to be run.
	 */
	private String optionalTestClass;
	private String optionalTestMethod;
	
	/**
	 * if Microbat is not parsing the Java code in eclipse, then, the user need to define paths
	 * of source code and test source code.
	 */
	private String soureCodePath;
	private String testCodePath;
	
	private List<String> additionalSourceFolders = new ArrayList<>();
	
	private SystemPreferences preferences;
	private ClassLoader classLoader;

	public AppJavaClassPath() {
		classpaths = new ArrayList<>();
		preferences = new SystemPreferences();
	}

	public String getJavaHome() {
		return javaHome;
	}

	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}

	public List<String> getClasspaths() {
		return new ArrayList<String>(classpaths);
	}

	public void addClasspaths(List<String> paths) {
		classpaths.addAll(paths);
	}

	public void addClasspath(String path) {
		classpaths.add(path);
	}
	
	public String getClasspathStr() {
		return StringUtils.join(classpaths, File.pathSeparator);		
	}
	
	public SystemPreferences getPreferences() {
		return preferences;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getOptionalTestClass() {
		return optionalTestClass;
	}

	public void setOptionalTestClass(String optionalTestClass) {
		this.optionalTestClass = optionalTestClass;
	}

	public String getOptionalTestMethod() {
		return optionalTestMethod;
	}

	public void setOptionalTestMethod(String optionalTestMethod) {
		this.optionalTestMethod = optionalTestMethod;
	}

	public String getLaunchClass() {
		return this.launchClass;
	}

	public void setLaunchClass(String launchClass) {
		this.launchClass = launchClass;
	}

	public String getSoureCodePath() {
		return soureCodePath;
	}

	public void setSourceCodePath(String soureCodePath) {
		this.soureCodePath = soureCodePath;
	}

	public String getTestCodePath() {
		return testCodePath;
	}

	public void setTestCodePath(String testCodePath) {
		this.testCodePath = testCodePath;
	}

	public List<String> getExternalLibPaths() {
		return externalLibPaths;
	}

	public void setExternalLibPaths(List<String> externalLibPaths) {
		this.externalLibPaths = externalLibPaths;
	}

	public void addExternalLibPath(String lib) {
		this.externalLibPaths.add(lib);
	}

	public String getAgentLib() {
		return agentLib;
	}

	public void setAgentLib(String agentLib) {
		this.agentLib = agentLib;
	}

	public List<String> getAgentBootstrapPathList() {
		return agentBootstrapPathList;
	}

	public void setAgentBootstrapPathList(List<String> agentBootstrapPathList) {
		this.agentBootstrapPathList = agentBootstrapPathList;
	}

	public List<String> getAdditionalSourceFolders() {
		return additionalSourceFolders;
	}

	public void setAdditionalSourceFolders(List<String> additionalSourceFolders) {
		this.additionalSourceFolders = additionalSourceFolders;
	}

	public List<String> getAllSourceFolders(){
		List<String> candidateSourceFolders = new ArrayList<>();
		candidateSourceFolders.add(getSoureCodePath());
		candidateSourceFolders.add(getTestCodePath());
		for(String additionalFolder: getAdditionalSourceFolders()){
			String path = additionalFolder;
			candidateSourceFolders.add(path);
		}
		
		return candidateSourceFolders;
	}

	public ClassLoader getClassLoader() {
		if (classLoader == null) {
			classLoader = initClassLoader();
		}
		return classLoader;
	}

	private ClassLoader initClassLoader() {
		try {
			List<URL> urlList = new ArrayList<URL>();
			for (String path : classpaths) {
				URL url = new File(path).toURI().toURL();
				urlList.add(url);
			}
			URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
			return new URLClassLoader(urls, this.getClass().getClassLoader());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
}
