/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.common;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.ObjectUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.BreakPoint;
import sav.strategies.dto.BreakPoint.Variable;

/**
 * @author LLT
 *
 */
public class VarInheritCustomizer implements IBreakpointCustomizer {
	private InheritType inheritType;
	
	public VarInheritCustomizer(InheritType varInheritType) {
		this.inheritType = varInheritType;
	}

	@Override
	public void customize(HashMap<String, BreakPoint> bkpMap) {
		customize(bkpMap.values());
	}

	private void customize(Collection<BreakPoint> bkps) {
		Map<String, List<BreakPoint>> bkpMap = new HashMap<String, List<BreakPoint>>();
		for (BreakPoint bkp : bkps) {
			CollectionUtils.getListInitIfEmpty(bkpMap, getClassMethod(bkp)).add(bkp);
		}
		Comparator<BreakPoint> comparator = getMethodLinesComparator(inheritType);
		for (List<BreakPoint> methodLines : bkpMap.values()) {
			Collections.sort(methodLines, comparator);
			List<Variable> previousVars = Collections.emptyList();
			for (BreakPoint bkp : methodLines) {
				bkp.addVars(previousVars);
				previousVars = bkp.getVars();
			}
		}
	}

	private Comparator<BreakPoint> getMethodLinesComparator(
			InheritType inheritType) {
		if (inheritType == InheritType.BACKWARD) {
			return new Comparator<BreakPoint>() {

				@Override
				public int compare(BreakPoint o1, BreakPoint o2) {
					return ObjectUtils.compare(o1.getLineNo(), o2.getLineNo());
				}
			};
		} else {
			return new Comparator<BreakPoint>() {

				@Override
				public int compare(BreakPoint o1, BreakPoint o2) {
					return ObjectUtils.compare(o2.getLineNo(), o1.getLineNo());
				}
			};
		}
	}

	private String getClassMethod(BreakPoint bkp) {
		return new StringBuilder(bkp.getClassCanonicalName()).append(".")
				.append(bkp.getMethodSign()).toString();
	}

	public static enum InheritType {
		BACKWARD,
		FORWARD;
		
		public static InheritType of(String name) {
			if (StringUtils.isEmpty(name)) {
				return null;
			}
			return valueOf(name);
		}
	}
}
