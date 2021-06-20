/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import sav.common.core.Constants;
import sav.common.core.Pair;


/**
 * @author LLT
 *
 */
public class ClassUtils {
	private ClassUtils() {}

	public static String getCanonicalName(String pkg, String clName) {
		return StringUtils.dotJoin(pkg, clName);
	}
	
	public static String toClassCanonicalName(String classPath) {
		return classPath.replace(Constants.FILE_SEPARATOR, Constants.DOT);
	}
	
	/**
	 * very weak method. only handle very simple case of className.
	 * TODO LLT: handle for the case of inner class as well.
	 */
	public static String getJFilePath(String sourcePath, String className) {
		return sourcePath + Constants.FILE_SEPARATOR
				+ className.replace(Constants.DOT, Constants.FILE_SEPARATOR)
				+ Constants.JAVA_EXT_WITH_DOT;
	}
	
	public static String getPackageFolderPath(String sourcePath, String className) {
		String packageName = className.substring(0, className.lastIndexOf("."));
		return sourcePath + Constants.FILE_SEPARATOR
				+ packageName.replace(Constants.DOT, Constants.FILE_SEPARATOR);
	}
	
	public static String getJFilePath(String className) {
		return className.replace(Constants.DOT, Constants.FILE_SEPARATOR)
				+ Constants.JAVA_EXT_WITH_DOT;
	}
	
	public static String getClassFilePath(String targetPath, String className) {
		return new StringBuilder()
						.append(targetPath)
						.append(Constants.FILE_SEPARATOR)
						.append(replaceDotWithFileSeparator(className))
						.append(Constants.CLASS_EXT_WITH_DOT)
						.toString();
	}
	
	public static List<File> getCompiledClassFiles(String targetPath,
			String className) {
		int lastDotIdx = className.lastIndexOf(Constants.DOT);
		String classSimpleName = className.substring(lastDotIdx + 1)
									.split("$")[0];
		String pkgName = className.substring(0, lastDotIdx);
		String classFolder = new StringBuilder()
								.append(targetPath)
								.append(Constants.FILE_SEPARATOR)
								.append(replaceDotWithFileSeparator(pkgName))
								.toString();
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(new File(classFolder), 
				new WildcardFileFilter(
						new String[]{getClassFileName(classSimpleName),
								getClassFileName(classSimpleName + "$*")}), null);
		return new ArrayList<File>(files);
	}
	
	private static String replaceDotWithFileSeparator(String str) {
		return str.replace(Constants.DOT, Constants.FILE_SEPARATOR);
	}
	
	public static String getClassFileName(String classSimpleName) {
		return classSimpleName + Constants.CLASS_EXT_WITH_DOT;
	}
	
	public static String getSimpleName(String className) {
		int idx = className.lastIndexOf(Constants.DOT);
		if (idx > 0) {
			return className.substring(idx + 1);
		}
		return className;
	}
	
	/**
	 * return pair of class name, and method name
	 */
	public static Pair<String, String> splitClassMethod(String name) {
		int idx = name.lastIndexOf(Constants.DOT);
		if (idx > 0) {
			return Pair.of(name.substring(0, idx), 
					name.substring(idx + 1));
		}
		throw new IllegalArgumentException(
				"Cannot extract method from string, expect [classname].[method], get "
						+ name);
	}
	
	public static String toClassMethodStr(Pair<String, String> classMethod) {
		return toClassMethodStr(classMethod.first(), classMethod.second());
	}
	
	public static String toClassMethodStr(String clazz, String method) {
		return StringUtils.dotJoin(clazz, method);
	}
	
	public static Class<?> getArrayContentType(Class<?> type) {
		Class<?> contentType = type;
		while (contentType.isArray()) {
			contentType = contentType.getComponentType();
		}
		if (contentType == type) {
			return null;
		}
		return contentType;
	}
	
	public static boolean isAupperB(Class<?> a, Class<?> b) {
		return a.isAssignableFrom(b);
	}

	public static List<Method> loockupMethodByNameOrSign(Class<?> clazz, String methodNameOrSign) {
		String methodName = SignatureUtils.extractMethodName(methodNameOrSign);
		String methodSign = SignatureUtils.extractSignature(methodNameOrSign);
		
		List<Method> matchingMethods = new ArrayList<Method>();
		/* try to look up by name first */
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				matchingMethods.add(method);
			}
		}
		
		if (matchingMethods.isEmpty()) {
			/* cannot find method for class */
			throw new IllegalArgumentException(String.format("cannot find method %s in class %s", methodNameOrSign
					, clazz.getName()));
		}
		
		/* if only one method is found with given name, just return. 
		 * otherwise, check for the method with right signature */
		if (matchingMethods.size() == 1) {
			return matchingMethods;
		}
		
		/*
		 * for easy case, just return the first one, if only method name is
		 * provided, and there are more than one method matches. Change the logic if necessary. 
		 */
		if (methodSign.isEmpty()) {
			return matchingMethods;
		}
		
		for (Method method : matchingMethods) {
			if (SignatureUtils.getSignature(method).equals(methodSign)) {
				return CollectionUtils.listOf(method, 1);
			}
		}
		
		/* no method in candidates matches the given signature */
		throw new IllegalArgumentException(String.format("cannot find method %s in class %s", methodNameOrSign
				, clazz.getName()));
	}
	
	/**
	 * The case which this function is missing to handle: Non-public top level class
	 * */
	public static String getCompilationUnitForSimpleCase(String className) {
		if (className.contains("$")) {
			return className.substring(0, className.indexOf("$"));
		} else {
			return className;
		}
	}

}
