/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Test;

import sav.common.core.Pair;

/**
 * @author LLT
 *
 */
public class JunitUtils {
	private static final String JUNIT_TEST_METHOD_PREFIX = "test";
	private static final String JUNIT_TEST_SUITE_PREFIX = "suite";
	
	public static List<String> extractTestMethods(List<String> junitClassNames, URLClassLoader classLoader)
			throws ClassNotFoundException {
		List<String> result = new ArrayList<String>();
		for (String className : junitClassNames) {
			extractTestMethods(result, className, classLoader);
		}
		return result;
	}

	private static void extractTestMethods(List<String> result, String className, URLClassLoader classLoader)
			throws ClassNotFoundException {
		Class<?> junitClass; 
		if(classLoader == null){
			junitClass = Class.forName(className);
		}
		else{
			junitClass = Class.forName(className, false, classLoader);
		}
		Method[] methods = junitClass.getDeclaredMethods();
		for (Method method : methods) {
			//TODO linyun: the condition should not be removed, I commented it just for debugging
			//if (isTestMethod(junitClass, method)) {
				result.add(ClassUtils.toClassMethodStr(className,
						method.getName()));
			//}
		}
		/* TODO: to remove. just for test the specific testcases in SIR */
		if (result.isEmpty()) {
			try {
				Method suiteMth = junitClass.getMethod(JUNIT_TEST_SUITE_PREFIX);
				TestSuite suite = (TestSuite) suiteMth.invoke(junitClass);
				findTestcasesInSuite(suite, result);
			} catch (Exception e) {
				throw new IllegalArgumentException("cannot find testcases in class " + className);
			}
			
		}
	}

	private static void findTestcasesInSuite(TestSuite suite,
			List<String> classMethods) throws ClassNotFoundException {
		Enumeration<junit.framework.Test> tests = suite.tests();
		while (tests.hasMoreElements()) {
			junit.framework.Test test = tests.nextElement();
			if (test instanceof TestSuite) {
				findTestcasesInSuite((TestSuite) test, classMethods);
			} else if (test instanceof TestCase) {
				TestCase tc = (TestCase) test;
				extractTestMethods(classMethods, tc.getClass().getName(), null);
			}
		}
	}

	public static boolean isTestMethod(Class<?> junitClass, Method method) {		
		Test test = method.getAnnotation(Test.class);
		if (test != null) {
			return true;
		}
		if (TestCase.class.isAssignableFrom(junitClass)) {
			int modifiers = method.getModifiers();
			if (Modifier.isPublic(modifiers)
					&& !Modifier.isStatic(modifiers)) {
				return true;
			}
		}
		return false;
	}
	
	public static List<Pair<String, String>> toPair(List<String> junitClassTestMethods) {
		List<Pair<String, String>> result = new ArrayList<Pair<String,String>>(junitClassTestMethods.size());
		for (String classMethod : junitClassTestMethods) {
			result.add(toPair(classMethod));
		}
		return result;
	}

	public static Pair<String, String> toPair(String junitClassTestMethod) {
		return ClassUtils.splitClassMethod(junitClassTestMethod);
	}
	
	public static List<String> toClassMethodStrs(List<Pair<String, String>> values) {
		List<String> result = new ArrayList<String>();
		for (Pair<String, String> value : values) {
			result.add(ClassUtils.toClassMethodStr(value));
		}
		return result;
	}
}
