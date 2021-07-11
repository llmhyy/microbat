/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.util;

import sav.common.core.utils.CollectionUtils;


/**
 * @author LLT
 *
 */
public class PrimitiveUtils {
	private PrimitiveUtils(){}
	private static String STRING_TYPE = String.class.getName();
	public static String T_BOOLEAN = "boolean";
	public static String T_CHAR = "char";
	public static String T_FLOAT = "float";
	public static String T_DOUBLE = "double";
	public static String T_BYTE = "byte";
	public static String T_SHORT = "short";
	public static String T_INT = "int";
	public static String T_LONG = "long";
	
	public static String[] PRIMITIVE_TYPES = new String[]{
		Integer.class.getName(),
		Boolean.class.getName(),
		Float.class.getName(),
		Character.class.getName(),
		Double.class.getName(),
		Long.class.getName(),
		Short.class.getName(),
		Byte.class.getName(),
		T_INT,
		T_BOOLEAN,
		T_FLOAT,
		T_CHAR,
		T_DOUBLE,
		T_LONG,
		T_SHORT,
		T_BYTE
	};
	
	public static boolean isPrimitiveType(String clazzName) {
		return CollectionUtils.existIn(clazzName, PRIMITIVE_TYPES);
	}
	
	public static boolean isPrimitive(String type){
		if(type.equals("int") ||
				type.equals("boolean") ||
				type.equals("float") ||
				type.equals("char") ||
				type.equals("double") ||
				type.equals("long") ||
				type.equals("short") ||
				type.equals("byte") ||
				type.equals(Integer.class.getCanonicalName()) ||
				type.equals(Boolean.class.getCanonicalName()) ||
				type.equals(Float.class.getCanonicalName()) ||
				type.equals(Double.class.getCanonicalName()) ||
				type.equals(Long.class.getCanonicalName()) ||
				type.equals(Short.class.getCanonicalName()) ||
				type.equals(Byte.class.getCanonicalName()) ||
				type.equals(Character.class.getCanonicalName())){
			return true;
		}
		
		return false;
	}
	
	public static boolean isPrimitiveTypeOrString(String clazzName) {
		return isPrimitiveType(clazzName) || isString(clazzName) 
				|| clazzName.equals("String") || clazzName.equals(STRING_TYPE);
	}

	public static boolean isString(String clazzName) {
		return STRING_TYPE.equals(clazzName);
	}
	
	
}
