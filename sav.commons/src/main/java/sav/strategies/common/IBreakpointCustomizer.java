/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.common;

import java.util.HashMap;

import sav.strategies.dto.BreakPoint;

/**
 * @author LLT
 *
 */
public interface IBreakpointCustomizer {

	void customize(HashMap<String, BreakPoint> bkpMap);

}
