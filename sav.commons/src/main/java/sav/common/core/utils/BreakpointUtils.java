/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sav.strategies.dto.BreakPoint;
import sav.strategies.dto.ClassLocation;

/**
 * @author LLT
 *
 */
public class BreakpointUtils {
	private BreakpointUtils(){}
	
	/**
	 * make the map of className and its breakpoint
	 */
	public static <T extends ClassLocation> Map<String, List<T>> initBrkpsMap(
			Collection<T> brkps) {
		Map<String, List<T>> brkpsMap = new HashMap<String, List<T>>();
		for (T brkp : brkps) {
			List<T> bps = CollectionUtils.getListInitIfEmpty(brkpsMap,
					brkp.getClassCanonicalName());
			bps.add(brkp);
		}
		return brkpsMap;
	}
	
	public static <T extends ClassLocation> Map<String, List<Integer>> initLineNoMap(
			List<T> brkps) {
		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
		for (T brkp : brkps) {
			List<Integer> lineNos = CollectionUtils.getListInitIfEmpty(map,
					brkp.getClassCanonicalName());
			lineNos.add(brkp.getLineNo());
		}
		return map;
	}
	
	public static <T extends ClassLocation> List<Integer> extractLineNo(List<T> bkps) {
		List<Integer> result = new ArrayList<Integer>(bkps.size());
		for (T loc : bkps) {
			result.add(loc.getLineNo());
		}
		return result;
	}
	
	public static <T extends ClassLocation>String getLocationId(T bkp) {
		return getLocationId(bkp.getClassCanonicalName(), bkp.getLineNo());
	}
	
	public static String getLocationId(String classNameOrPath, int lineNo) {
		return String.format("%s:%s", classNameOrPath.replace("/", "."), lineNo);
	}

	public static <T extends ClassLocation> List<String> toLocationIds(
			Collection<T> bkps) {
		List<String> locs = new ArrayList<String>(bkps.size());
		for (T bkp : bkps) {
			locs.add(getLocationId(bkp));
		}
		return locs;
	}

	public static String getPrintStr(Collection<BreakPoint> bkps) {
		return StringUtils.join(toLocationIds(bkps), ", ");
	}

	public static List<String> extractClasses(Set<BreakPoint> bkps) {
		Set<String> classNames = new HashSet<String>();
		for (BreakPoint bkp : bkps) {
			classNames.add(bkp.getClassCanonicalName());
		}
		return new ArrayList<String>(classNames);
	}
	
	public static List<BreakPoint> toBreakpoints(List<ClassLocation> locations) {
		List<BreakPoint> breakpoints = new ArrayList<BreakPoint>();
		for (ClassLocation location : locations) {
			BreakPoint breakpoint = toBreakPoint(location);
			breakpoints.add(breakpoint);
		}

		return breakpoints;
	}

	public static BreakPoint toBreakPoint(ClassLocation location) {
		BreakPoint breakpoint = new BreakPoint(
				location.getClassCanonicalName(), location.getMethodSign(),
				location.getLineNo());
		return breakpoint;
	}
	
}
