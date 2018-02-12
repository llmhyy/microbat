/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.mutanbug;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LLT
 * 
 */
public class DebugLineInsertionResult {
	private String className;
	private File mutatedFile;
	private Map<Integer, Integer> oldNewLocMap;
	
	public DebugLineInsertionResult(String className) {
		this.className = className;
		oldNewLocMap = new HashMap<Integer, Integer>();
	}
	
	public void mapDebugLine(int before, int after) {
		oldNewLocMap.put(before, after);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public File getMutatedFile() {
		return mutatedFile;
	}

	public void setMutatedFile(File mutatedFile) {
		this.mutatedFile = mutatedFile;
	}

	public Map<Integer, Integer> getOldNewLocMap() {
		return oldNewLocMap;
	}

	public void setOldNewLocMap(Map<Integer, Integer> oldNewLocMap) {
		this.oldNewLocMap = oldNewLocMap;
	}

	@Override
	public String toString() {
		return "DebugLineInsertionResult [className=" + className
				+ ", mutatedFile=" + mutatedFile + ", oldNewLocMap="
				+ oldNewLocMap + "]";
	}
	
}
