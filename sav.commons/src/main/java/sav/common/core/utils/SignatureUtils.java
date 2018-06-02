/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.lang.reflect.Method;

/**
 * @author LLT
 * 
 */
public class SignatureUtils {
	/**
     * Compute the JVM method descriptor for the method.
     */
	public static String getSignature(Method meth) {
		StringBuffer sb = new StringBuffer();

		sb.append("(");

		Class<?>[] params = meth.getParameterTypes(); // avoid clone
		for (int j = 0; j < params.length; j++) {
			sb.append(getSignature(params[j]));
		}
		sb.append(")");
		sb.append(getSignature(meth.getReturnType()));
		return sb.toString();
	}
	
	public static boolean isSignature(String signature){
		if(signature.length()==1){
			return true;
		}
		else{
			if((signature.startsWith("L") && signature.endsWith(";")) ||
					signature.startsWith("[")){
				return true;
			}
		}
		
		return false;
	}
	
	public static String signatureToName(String signature){
		signature = trimSignature(signature);
		
		String arrayString = "";
		int startArray = signature.indexOf("[");
		int endArray = signature.lastIndexOf("[");
		
		if(startArray != -1){
			for(int i=0; i<endArray-startArray+1; i++){
				arrayString += "[]";
			}
			signature = signature.substring(endArray+1, signature.length());
		}
		
		
		if (signature.equals("I")) {
			return "int" + arrayString;
		} else if (signature.equals("B")) {
			return "byte" + arrayString;
		} else if (signature.equals("J")) {
			return "long" + arrayString;
		} else if (signature.equals("F")) {
			return "float" + arrayString;
		} else if (signature.equals("D")) {
			return "double" + arrayString;
		} else if (signature.equals("S")) {
			return "short" + arrayString;
		} else if (signature.equals("C")) {
			return "char" + arrayString;
		} else if (signature.equals("Z")) {
			return "boolean" + arrayString;
		} else if (signature.equals("V")) {
			return "void";
		}
		
		signature = signature.substring(1, signature.length());
		signature = signature.replace("/", ".");
		signature += arrayString;
		
		return signature;
	}

	/**
	 * Compute the JVM signature for the class.
	 */
	public static String getSignature(Class<?> clazz) {
		String type = null;
		if (clazz.isArray()) {
			Class<?> cl = clazz;
			int dimensions = 0;
			while (cl.isArray()) {
				dimensions++;
				cl = cl.getComponentType();
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < dimensions; i++) {
				sb.append("[");
			}
			sb.append(getSignature(cl));
			type = sb.toString();
		} else if (clazz.isPrimitive()) {
			if (clazz == Integer.TYPE) {
				type = "I";
			} else if (clazz == Byte.TYPE) {
				type = "B";
			} else if (clazz == Long.TYPE) {
				type = "J";
			} else if (clazz == Float.TYPE) {
				type = "F";
			} else if (clazz == Double.TYPE) {
				type = "D";
			} else if (clazz == Short.TYPE) {
				type = "S";
			} else if (clazz == Character.TYPE) {
				type = "C";
			} else if (clazz == Boolean.TYPE) {
				type = "Z";
			} else if (clazz == Void.TYPE) {
				type = "V";
			}
		} else {
			type = getSignature(clazz.getName());
		}
		return type;
	}

	public static String getSignature(String className) {
		return "L" + className.replace('.', '/') + ";";
	}
	
	public static String extractMethodName(String methodNameOrSign) {
		int endNameIdx = methodNameOrSign.indexOf("(");
		if (endNameIdx < 0) {
			return methodNameOrSign;
		}
		String fullMethodName = methodNameOrSign.substring(0, endNameIdx);
		if (fullMethodName.contains(".")) {
			return fullMethodName.substring(fullMethodName.lastIndexOf(".")+1,
					fullMethodName.length());
		}
		return fullMethodName;
	}
	
	public static String extractSignature(String methodNameAndSign) {
		int endNameIdx = methodNameAndSign.indexOf("(");
		if (endNameIdx > 1) {
			return methodNameAndSign.substring(endNameIdx);
		}
		return methodNameAndSign;
	}
	
	public static String trimSignature(String typeSign) {
		return typeSign.replace(";", "");
	}

	public static String createMethodNameSign(String methodName, String signature) {
		return methodName + signature;
	}
	
	public static String createMethodNameSign(Method method) {
		return createMethodNameSign(method.getName(), getSignature(method));
	}
}
