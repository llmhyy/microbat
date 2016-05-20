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
 * @author LLT
 * 
 */
public class AppJavaClassPath {
	private String javaHome;
	private String workingDirectory;
	private Set<String> classpaths;
	private String src;
	private String target;
	private String testTarget;
	
	private String launchClass;
	
	private String optionalTestClass;
	private String optionalTestMethod;
	
	
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
	
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTestTarget() {
		return testTarget;
	}

	public void setTestTarget(String testTarget) {
		this.testTarget = testTarget;
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
}
