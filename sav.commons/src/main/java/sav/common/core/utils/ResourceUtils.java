/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.io.Closeable;

import sav.common.core.Constants;

/**
 * @author LLT
 *
 */
public class ResourceUtils {

	public static String appendPath(String...fragment) {
		return StringUtils.join(Constants.FILE_SEPARATOR, (Object[])fragment);
	}
	
	public static void closeQuitely(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
