/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.python;

/**
 * @author LLT
 *
 */
public class PythonVmConfiguration {
	private String pythonHome;
	private String launchClass;

	public String getPythonHome() {
		return pythonHome;
	}

	public void setPythonHome(String pythonHome) {
		this.pythonHome = pythonHome;
	}

	public String getLaunchClass() {
		return launchClass;
	}

	public void setLaunchClass(String launchClass) {
		this.launchClass = launchClass;
	}

}
