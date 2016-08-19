/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import sav.common.core.Constants;

/**
 * @author LLT
 * 
 */
public class StringUtils {
	public static final String EMPTY = org.apache.commons.lang.StringUtils.EMPTY;
	public static final String SPACE = " ";
	
	private StringUtils() {
	}
	
	public static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }
	
	public static String join(Collection<?> vals, String separator) {
		if (CollectionUtils.isEmpty(vals)) {
			return EMPTY;
		}
		List<String> valStrs = new ArrayList<String>();
		for (Object val : vals) {
			String valStr = toStringNullToEmpty(val);
			if (!isEmpty(valStr)) {
				valStrs.add(valStr);
			}
		}
		return org.apache.commons.lang.StringUtils.join(valStrs, separator);
	}

	public static String join(String separator, Object... params) {
		return join(Arrays.asList(params), separator);
	}
	
	public static String newLineJoin(List<?> params) {
		return join(params, Constants.NEW_LINE);
	}
	
	public static String spaceJoin(Object...params) {
		return join(SPACE, params);
	}
	
	public static String dotJoin(Object... params) {
		return join(Arrays.asList(params), ".");
	}
	
	public static String toStringNullToEmpty(Object val) {
		return toString(val, EMPTY);
	}
	
	public static String toString(Object val, String defaultIfNull) {
		if (val == null) {
			return defaultIfNull;
		}
		return val.toString();
	}
	
	public static String nullToEmpty(String val) {
		if (val == null) {
			return EMPTY;
		}
		return val;
	}
	
	public static String[] nullToEmpty(String[] val) {
		if (val == null) {
			return new String[0];
		}
		return val;
	}
	
	public static boolean isStartWithUppercaseLetter(String text) {
		if (org.apache.commons.lang.StringUtils.isEmpty(text)) {
			return false;
		}
		return Character.isUpperCase(text.charAt(0));
	}

	public static String enumToString(String enumClzz, Object enumVal) {
		return dotJoin(enumClzz, ((Enum<?>) enumVal).name());
	}

	/**
	 * @param packageName
	 *            the package name as specified in the package declaration (i.e.
	 *            a dot-separated name)
	 * @param simpleTypeName
	 *            the simple name of the type
	 * @param enclosingTypeNames
	 *            if the type is a member type, the simple names of the
	 *            enclosing types from the outer-most to the direct parent of
	 *            the type (for example, if the class is x.y.A$B$C then the
	 *            enclosing types are [A, B]. This is an empty array if the type
	 *            is a top-level type.
	 */
	public static String dotJoinStr(char[] packageName,
			char[][] enclosingTypeNames, char[] simpleTypeName) {
		Object[] enclosing = new Object[enclosingTypeNames.length];
		for (int i = 0; i < enclosingTypeNames.length; i++) {
			enclosing[i] = new String(enclosingTypeNames[i]);
		}
		return dotJoin(new String(packageName), dotJoin(enclosing),
				new String(simpleTypeName));
	}
}
