/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sav.common.core.utils.StringUtils;

/**
 * @author LLT, modified by Yun Lin.
 * 
 */
public class AppJavaClassPath {
	private String javaHome;
	private String workingDirectory;
	private Set<String> classpaths;
	
	private List<String> externalLibPaths = new ArrayList<>();
	
	private String launchClass;
	
	private String agentLib;
	
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
	
	private SystemPreferences preferences;

	public AppJavaClassPath() {
		classpaths = new HashSet<String>();
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

}
