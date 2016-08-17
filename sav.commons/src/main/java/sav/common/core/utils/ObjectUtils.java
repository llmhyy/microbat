/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;



/**
 * @author LLT
 * 
 */
public class ObjectUtils {

	public static <T>boolean equal(T[] a0, T[] a1) {
		if (a0 == a1) {
			return true;
		}
		if (a0 == null || a1 == null) {
			return false;
		}
		if (a0.length != a1.length) {
			return false;
		}
		for (int i = 0; i < a0.length; i++) {
			if (!ObjectUtils.equalsWithNull(a0[i], a1[i])) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean safeEquals(Object a, Object b) {
		return a != null && a.equals(b);
	}

	public static boolean equalsWithNull(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}
	
	public static int compare(int o1, int o2) {
		return (o1 < o2 ? -1 : (o1 == o2 ? 0 : 1));
	}
	
	public static Class<?> getObjClass(Object obj, Class<?> defaultIfNull) {
		if (obj == null) {
			return defaultIfNull;
		}
		if (obj instanceof Class) {
			return (Class<?>) obj;
		}
		return obj.getClass();
	}
	
	public static boolean isPositive(int val) {
		if (val > 0) {
			return true;
		}
		return false;
	}
	
	public static <T> T returnValueOrAlt(T value, T altValue) {
		if (value == null) {
			return altValue;
		}
		return value;
	}
	
	public static boolean toBoolean(String val, boolean defaultIfEmpty) {
		if (StringUtils.isEmpty(val)) {
			return defaultIfEmpty;
		}
		return Boolean.valueOf(val);
	}
}
