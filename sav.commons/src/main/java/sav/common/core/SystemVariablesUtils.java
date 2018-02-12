/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core;

import static sav.common.core.SystemVariables.SYS_SAV_JUNIT_JAR;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.junit.SavJunitRunner;


/**
 * @author LLT
 *
 */
public class SystemVariablesUtils {
	private SystemVariablesUtils(){}
	
	public static String updateSavJunitJarPath(AppJavaClassPath appClasspath) {
		String jarPath = appClasspath.getPreferences().get(SYS_SAV_JUNIT_JAR);
		if (jarPath == null) {
			jarPath = SavJunitRunner.extractToTemp().getAbsolutePath();
			appClasspath.getPreferences().put(SYS_SAV_JUNIT_JAR, jarPath);
		}
		return jarPath;
	}
	
}
