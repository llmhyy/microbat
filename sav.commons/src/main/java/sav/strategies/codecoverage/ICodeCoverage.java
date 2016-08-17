/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.codecoverage;

import java.util.List;

/**
 * @author LLT
 *
 */
public interface ICodeCoverage {
	public void run(ICoverageReport reporter, List<String> testingClassNames,
			List<String> junitClassNames) throws Exception;
}
