/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.SignatureUtils;

/**
 * @author LLT
 *
 */
public class MethodSignatureGetter {
	
	public static void main(String[] args) {
		printSignature(MethodSignatureGetter.class, new String[]{"main"});
	}

	private static void printSignature(Class<?> clazz, String[] methodNames) {
		List<List<String>> signs = getSignature(clazz, methodNames);
		for (List<String> sign : signs) {
			for (String s : sign) {
				System.out.println(s);
			}
		}
	}
	
	public static List<List<String>> getSignature(Class<?> clazz,
			String[] methodNames) {
		List<List<String>> result = new ArrayList<List<String>>();
		for (String methodName : methodNames) {
			result.add(getSignature(clazz, methodName)); 
		}
		return result;
	}

	public static List<String> getSignature(Class<?> clazz, String methodName) {
		List<String> signs = new ArrayList<String>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				signs.add(SignatureUtils.createMethodNameSign(method));
			}
		}
		return signs;
	}
}
