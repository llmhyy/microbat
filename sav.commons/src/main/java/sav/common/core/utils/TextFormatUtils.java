/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.util.Collection;

/**
 * @author LLT
 * 
 */
public class TextFormatUtils {
	private TextFormatUtils() {
	}
	
	public static <T>String printListSeparateWithNewLine(Collection<T> values) {
		return StringUtils.join(values, "\n");
	}
}
