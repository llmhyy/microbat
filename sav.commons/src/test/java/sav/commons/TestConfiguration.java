/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons;

import static sav.common.core.utils.ResourceUtils.appendPath;
import sav.common.core.Constants;
import sav.common.core.utils.ConfigUtils;

/**
 * @author LLT
 * 
 */
public class TestConfiguration {
	private static final String PRJ_APP = "app";
	private static final String PRJ_ETC = "etc";
	private static TestConfiguration config = new TestConfiguration();
	public static final String TRUNK;
	public static final String APP;
	public static final String ETC;
	public static String SAV_COMMONS_TEST_TARGET;
	public String javaSlicerPath;
	// do not remove this one, this is not the current java home of the
	// application
	public static String JAVA_HOME;
	public static String TESTDATA_CSV;
	public static String TESTDATA_PROPERTIES;
	
	static {
		TRUNK = getTrunk();
		ETC = appendPath(TRUNK, PRJ_ETC);
		APP = appendPath(TRUNK, PRJ_APP);
		SAV_COMMONS_TEST_TARGET = getTestTarget("sav.commons");
		JAVA_HOME = getJavaHome();
		TESTDATA_CSV  = getEtcResources("testdata.csv");
		TESTDATA_PROPERTIES = getEtcResources("testdata.properties");
	}

	private static String getTrunk() {
		String userdir = System.getProperty("user.dir");
		int indexOfapp = userdir.indexOf(Constants.FILE_SEPARATOR + PRJ_APP);
		if (indexOfapp < 0) {
			return ConfigUtils.getProperty("trunk");
		}
		return userdir.substring(0, indexOfapp);
	}

	private TestConfiguration() {
	}
	
	public static String getTestScrPath(String module) {
		return appendPath(APP, module, "src/test/java");
	}

	public static String getTestTarget(String module) {
		return appendPath(APP, module, "target/test-classes");
	}

	public static String getTarget(String module) {
		return appendPath(APP, module, "target/classes");
	}
	
	public static String getTestResources(String module) {
		return appendPath(APP, module, "src/test/resources");
	}

	public static TestConfiguration getInstance() {
		return config;
	}
	
	public static String getEtcResources(String fileName) {
		return appendPath(ETC, fileName);
	}

	public String getJavaBin() {
		return JAVA_HOME + "/bin";
	}

	/* work around for getting jdk, not jre */
	public static String getJavaHome() {
		// work around in case java home not point to jdk but jre.
		String javaHome = System.getProperty("java.home");
		if (javaHome.endsWith("jre")) {
			javaHome = javaHome.substring(0,
					javaHome.lastIndexOf(Constants.FILE_SEPARATOR + "jre"));
		}
		return javaHome;
	}
}
