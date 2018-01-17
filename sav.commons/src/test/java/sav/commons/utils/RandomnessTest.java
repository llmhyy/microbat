/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import sav.common.core.iface.HasProbabilityType;
import sav.common.core.utils.Randomness;

/**
 * @author LLT
 *
 */
public class RandomnessTest {

	@Test
	public void testRandomSubList() {
		List<Integer> allList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		for (int i = 0; i < 100; i++) {
			System.out.println(Randomness.randomSubList(allList));
		}
	}
	
	@Test
	public void testRandomSubListFixSize() {
		List<Integer> allList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		for (int i = 0; i < 100; i++) {
			System.out.println(Randomness.randomSubList(allList, 5));
		}
	}
	
	@Test
	public void testRandomInt() {
		for (int i = 0; i < 100; i++) {
			System.out.println(Randomness.nextInt(15));
		}
	}
	
	@Test
	public void testRandomWithDistribution() {
		HasProbabilityType[] eles = TypeWithProbability.values();
		for (int i = 0; i < 100; i++) {
			System.out.println(Randomness.randomWithDistribution(eles));
		}
	}
	
	private static enum TypeWithProbability implements HasProbabilityType {
		TYPE1(10),
		TYPE2(4),
		TYPE3(2),
		TYPE4(1);
		
		private int prob;
		private TypeWithProbability(int prob) {
			this.prob = prob;
		}
		
		@Override
		public int getProb() {
			return prob;
		}
		
	}
}
