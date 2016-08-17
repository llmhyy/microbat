/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons;

import static sav.commons.TestConfiguration.SAV_COMMONS_TEST_TARGET;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;

import sav.common.core.utils.StringUtils;
import sav.commons.testdata.opensource.TestPackage;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.SystemPreferences;
import sav.strategies.vm.VMConfiguration;


/**
 * @author LLT
 * 
 */

public class AbstractTest {
	public static final String SAV_COMMONS = "sav.commons";
	protected static final String ICSETLV = "icsetlv";
	protected static TestConfiguration config = TestConfiguration.getInstance();
	
	@BeforeClass
	public static void init() throws Exception {
		ResourceBundle log4jtest = ResourceBundle.getBundle("test-log4j");
		Properties props = new Properties();
		for (String key : log4jtest.keySet()) {
			props.setProperty(key, log4jtest.getString(key));
		}
		PropertyConfigurator.configure(props);
	}
	
	public void print(Object... objs) {
		System.out.println(StringUtils.spaceJoin(objs));
	}

	public <T>void printList(List<T> list) {
		for (T ele : list) {
			System.out.println(ele);
		}
	}
	
	protected AppJavaClassPath initAppClasspath() {
		AppJavaClassPath appClasspath = new AppJavaClassPath();
		appClasspath.setJavaHome(TestConfiguration.getJavaHome());
		appClasspath.addClasspath(SAV_COMMONS_TEST_TARGET);
		return appClasspath;
	}

	protected VMConfiguration initVmConfig() {
		return new VMConfiguration(initAppClasspath());
	}
	
	protected List<String> getLibJars(String... libFolders) throws Exception {
		List<String> jars = new ArrayList<String>();
		for (String libFolder : libFolders) {
			Collection<?> files = FileUtils.listFiles(new File(libFolder),
					new String[] { "jar" }, true);
			for (Object obj : files) {
				File file = (File) obj;
				jars.add(file.getAbsolutePath());
			}
		}
		return jars;
	}
	
	protected void updateSystemClasspath(List<String> classpaths)
			throws Exception {
		for (String path : classpaths) {
			addToSysClassLoader(new File(path));
		}
	}
	
	public static void addToSysClassLoader(File file) throws Exception {
	    Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
	    method.setAccessible(true);
	    method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
	}
	
	public AppJavaClassPath initAppClasspath(TestPackage pkg) {
		AppJavaClassPath appClasspath = new AppJavaClassPath();
		appClasspath.setJavaHome(TestConfiguration.JAVA_HOME);
		appClasspath.addClasspaths(pkg.getClassPaths());
		appClasspath.addClasspaths(pkg.getLibFolders());
		return appClasspath;
	}
	
	protected final void loadPreferences(SystemPreferences preferences, String configStr) {
		String[] configs = configStr.split("\n");
		for (String config : configs) {
			String[] keyValue = config.split("=");
			preferences.put(keyValue[0].trim(), keyValue[1].trim());
		}
	}
}
