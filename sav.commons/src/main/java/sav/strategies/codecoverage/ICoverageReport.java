/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.codecoverage;

import java.util.List;

import sav.common.core.Pair;
import sav.strategies.dto.BreakPoint;

/**
 * @author LLT
 *
 */
public interface ICoverageReport {

	public void addFailureTrace(final List<BreakPoint> traces);

	public void addInfo(final int testcaseIndex, final String className,
			final int lineIndex, final boolean isPassed, final boolean isCovered);

	public void setTestingClassNames(List<String> testingClassNames);

	public void setFailTests(List<Pair<String, String>> failTests);
}
