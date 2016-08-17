/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.runner.JUnitCore;

import sav.common.core.SystemVariablesUtils;
import sav.common.core.SavRtException;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.BreakPoint;
import sav.strategies.vm.VMConfiguration;

/**
 * @author LLT
 *
 */
public class SavJunitRunner {
	private static final String RESOURCE = "/sav.junit.runner.jar";
	public static final BreakPoint ENTER_TC_BKP;
	public static final String JUNIT_RUNNER_CLASS_NAME = JunitRunner.class.getName();
	
	static {
		/* at junit 4.0 */
		ENTER_TC_BKP = new BreakPoint(JUnitCore.class.getName(), 138);
	}

	public static File extractToTemp() throws SavRtException {
		File jar;
		try {
			jar = File.createTempFile("sav.junit.runner", ".jar");
			jar.deleteOnExit();
			extractTo(jar);
		} catch (Exception e) {
			throw new SavRtException(e);
		}
		return jar;
	}

	public static void extractTo(File destFile) throws FileNotFoundException, IOException {
		InputStream inStream = SavJunitRunner.class.getResourceAsStream(RESOURCE);
		IOUtils.copy(inStream, new FileOutputStream(destFile));
	}

	public static VMConfiguration createVmConfig(AppJavaClassPath appClasspath) {
		VMConfiguration vmConfig = new VMConfiguration(appClasspath);
		String savJunitJar = SystemVariablesUtils.updateSavJunitJarPath(appClasspath);
		vmConfig.addClasspath(savJunitJar);
		
		System.currentTimeMillis();
		return vmConfig;
	}
}
