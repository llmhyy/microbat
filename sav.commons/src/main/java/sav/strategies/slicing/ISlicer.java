/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.slicing;


import java.util.List;

import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.BreakPoint;


/**
 * @author LLT
 *
 */
public interface ISlicer {

	List<BreakPoint> slice(AppJavaClassPath appClassPath,
			List<BreakPoint> entryPoints, List<String> junitClassNames)
			throws Exception;
	
	void setFiltering(List<String> analyzedClasses, List<String> analyzedPackages);
}
