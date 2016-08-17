/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.DebugLineInsertionResult;
import testdata.insertion.InsertTestData;

import mutation.mutator.Mutator;


/**
 * @author LLT
 *
 */
public class DebugLineInsertionTest {
	private Mutator mutator;
	
	@Before
	public void setup() {
		String srcFolder = "./src/test/java";
		mutator = new Mutator(srcFolder);
	}
	
	public DebugLineInsertionResult runTestInsertion(int... lines) {
		Map<String, List<ClassLocation>> classLocationMap = new HashMap<String, List<ClassLocation>>();
		String clazzName = InsertTestData.class.getName();
		List<ClassLocation> value = new ArrayList<ClassLocation>();
		for (int line : lines) {
			value.add(new ClassLocation(clazzName, null, line));
		}
		classLocationMap.put(clazzName, value);
		Map<String, DebugLineInsertionResult> result = mutator
				.insertDebugLine(classLocationMap);
		System.out.println(result);
		return result.get(clazzName);
	}
	
	@Test
	public void testInsertion() {
		DebugLineInsertionResult result = runTestInsertion(25, 27, 30, 31, 32, 42, 46, 50);
		Assert.assertEquals("{50=56, 32=36, 42=46, 25=26, 27=29, 46=50, 31=34, 30=32}", 
				result.getOldNewLocMap().toString());
	}
	
	@Test
	public void testSpecialCondStmt() {
		DebugLineInsertionResult result = runTestInsertion(55, 56, 61);
		
	}
	
	@Test
	public void testLineInsideLoop() {
		DebugLineInsertionResult result = runTestInsertion(27, 28, 29, 34, 35);
		System.out.println(result.getOldNewLocMap().toString());
		Assert.assertEquals("{34=39, 35=39, 27=31, 29=31, 28=31}", result.getOldNewLocMap().toString());
	}
}
