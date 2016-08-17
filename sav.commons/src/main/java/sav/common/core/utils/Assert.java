/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author LLT
 * If the exception because of Assertion error, means it needs to be fixed.
 */
public class Assert {
	private static Logger log = LoggerFactory.getLogger(Assert.class);
	
	public static <T> void notNull(T value, String... msgs) {
		assertTrue(value != null, msgs);
	}

	public static <T> void assertTrue(boolean condition, String... msgs) {
		if (!condition) {
			String msg = StringUtils.EMPTY;
			if (msgs != null) {
				msg = StringUtils.spaceJoin((Object[]) msgs);
			}
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
	}
	
	public static void assertNotNull(Object obj, String... msgs) {
		assertTrue(obj != null, msgs);
	}


	public static void fail(String msg) {
		throw new IllegalArgumentException(msg);
	}

}
