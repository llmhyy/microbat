/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import sav.common.core.Constants;

/**
 * @author LLT
 * 
 */
public class StringUtils {
	public static final String EMPTY = "";
	public static final String SPACE = " ";
	
	private StringUtils() {
	}
	
	public static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }
	
	public static String join(Collection<?> vals, String separator) {
		if (vals == null || vals.isEmpty()) {
			return EMPTY;
		}
		StringBuilder varSb = new StringBuilder();
		int i = 0;
		for (Object val : vals) {
			i ++;
			if (val != null) {
				varSb.append(val.toString());
				if (i != vals.size()) {
					varSb.append(separator);
				}
			}
		}
		String str = varSb.toString();
		if (str.endsWith(separator)) {
			return str.substring(0, str.length() - separator.length());
		}
		return str;
	}

	public static String join(String separator, Object... params) {
		return join(Arrays.asList(params), separator);
	}
	
	/**
	 * return a join string with quote for each value.
	 */
	public static String toJoinStrCode(List<?> allVars) {
		StringBuilder varSb = new StringBuilder();
		for (int i = 0; i < allVars.size(); i++) {
			Object val = allVars.get(i);
			if (val == null) {
				varSb.append("null");
			} else {
				varSb.append("\"").append(val.toString()).append("\"");;
			}
			if (i != allVars.size() - 1) {
				varSb.append(", ");
			}
		}
		return varSb.toString();
	}
	
	public static String newLineJoin(Collection<?> params) {
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
	
	public static String emptyToNull(String val) {
		if (val != null && val.isEmpty()) {
			return null;
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
		if (isEmpty(text)) {
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
	
	public static String[] dotSplit(String str) {
		return str.split("\\.");
	}
	
	public static List<String> sortAlphanumericStrings(List<String> list) {
		Collections.sort(list, new AlphanumComparator());
		return list;
	}
	
	public static String subString(String str, String startToken, String endToken) {
		int si = str.indexOf(startToken);
		if (si < 0) {
			return EMPTY;
		}
		si += startToken.length();
		int ei = str.indexOf(endToken, si);
		if (ei < 0) {
			return EMPTY;
		}
		String subStr = str.substring(si, ei).trim();
		return subStr;
	}
}
