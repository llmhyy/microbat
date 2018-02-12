/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.gentest;



/**
 * @author LLT
 *
 */
public interface ISubTypesScanner {
	/**
	 * return an implementation for the interface. will be picked up randomly.
	 */
	public Class<?> getRandomImplClzz(Class<?> clazz);

	public Class<?> getRandomImplClzz(Class<?>[] bounds);
}
