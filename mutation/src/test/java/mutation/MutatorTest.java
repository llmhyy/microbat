/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mutation.mutator.Mutator;

import org.junit.Before;
import org.junit.Test;

import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.MutationResult;
import testdata.mutator.Main;
import testdata.mutator.MutationTestData;

/**
 * @author LLT
 *
 */
public class MutatorTest {
	private Mutator mutator;
	
	@Before
	public void setup() {
		String srcFolder = "./src/test/java";
		mutator = new Mutator(srcFolder);
	}
	
	@Test
	public void testMutator() {
		String clazzName = MutationTestData.class.getName();
		clazzName = Main.class.getName();
//		clazzName = FastMath.class.getName();
		List<ClassLocation> value = new ArrayList<ClassLocation>();
		value.add(new ClassLocation(clazzName, null, 2606));
//		value.add(new ClassLocation(clazzName, null, 27));
//		value.add(new ClassLocation(clazzName, null, 33));
//		value.add(new ClassLocation(clazzName, null, 35));
		Map<String, MutationResult> result = mutator.mutate(value);
		System.out.println(result);
	}
	
//	public static void main(String[] args){
//		System.currentTimeMillis();
//	}
}
