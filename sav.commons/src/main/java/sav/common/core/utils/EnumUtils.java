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
public class EnumUtils {
	private EnumUtils(){}
	
	/**
	 * return 
	 * [type.name].[val.name]
	 */
	public static String getFullName(Enum<?> val) {
		return StringUtils.dotJoin(val.getDeclaringClass().getName(),
				val.name());
	}
	
	/**
	 * return 
	 * [type.simpleName].[val.name]
	 */
	public static String getName(Enum<?> val) {
		return StringUtils.dotJoin(val.getDeclaringClass().getSimpleName(), 
				val.name());
	}
}
