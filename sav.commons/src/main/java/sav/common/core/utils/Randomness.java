/**
 * Copyright TODO
 */
package sav.common.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sav.common.core.iface.HasProbabilityType;

/**
 * @author LLT
 * for centralization
 */
public final class Randomness {
	private Randomness() {}

	public static final long SEED = System.nanoTime();
	public static int totalCallsToRandom = 0;
	static Random random = new Random(SEED);
	
	private static Random getRandom() {
		totalCallsToRandom++;
		return random;
	}

	public static void reset(long newSeed) {
		random = new Random(newSeed);
	}

	public static boolean nextBoolean() {
		return getRandom().nextBoolean();
	}

	public static float nextFloat() {
		return getRandom().nextFloat();
	}
	
	public static int nextInt() {
		return getRandom().nextInt();
	}
	
	public static Long nextLong() {
		return getRandom().nextLong();
	}
	
	/**
	 * return random value from 0 to i - 1 
	 */
	public static int nextInt(int i) {
		return getRandom().nextInt(i);
	}
	
	public static int nextInt(int min, int max) {
		return getRandom().nextInt((max - min) + 1) + min;
	}
	
	public static <T> T randomMember(T[] arr) {
		if (CollectionUtils.isEmpty(arr)) {
			return null;
		}
		return arr[nextInt(arr.length)];
	}

	public static <T> T randomMember(List<T> list) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(nextInt(list.size()));
	}

	public static <T> List<T> randomSubList(List<T> allList) {
		return randomSubList(allList, nextInt(allList.size()));
	}
	
	public static <T> List<T> randomSubList(T[] allArr) {
		return randomSubList(Arrays.asList(allArr));
	}
	
	public static <T> List<T> randomSubListKeepOder(List<T> allList, int subSize) {
		List<T> sublist = new ArrayList<T>(allList);
		for (int i = 0; i < subSize; i++) {
			sublist.remove(nextInt(sublist.size()));
		}
		return sublist;
	}
	
	public static <T> List<T> randomSequence(T[] allArr, int seqSize) {
		return randomSequenceFixSize(Arrays.asList(allArr), seqSize);
	}
	
	public static <T> List<T> randomSequenceFixSize(List<T> allList, int seqSize) {
		List<T> seq = new ArrayList<T>(seqSize);
		for (int i = 0; i < seqSize; i++) {
			seq.add(allList.get(nextInt(allList.size())));
		}
		return seq;
	}
	
	public static <T> List<T> randomSequence(List<T> allList, int maxSeqSize) {
		if (allList.isEmpty()) {
			return allList;
		}
		return randomSequenceFixSize(allList, nextInt(maxSeqSize + 1));
	}
	
	/**
	 * This return random sublist without duplicate idx.
	 * */
	public static <T> List<T> randomSubList(List<T> allList, int subSize) {
		List<T> sublist = new ArrayList<T>();
		int n = allList.size();
		int[] swaps = new int[allList.size()];
		for (int i = 0; i < subSize; i++) {
			int nextIdx = nextInt(n);
			int realIdx = swaps[nextIdx];
			while(realIdx != 0) {
				nextIdx = realIdx - 1;
				realIdx = swaps[realIdx - 1];
			}
			sublist.add(allList.get(nextIdx));
			swaps[nextIdx] = n;
			n--;
		}
		return sublist;
	}
	
	public static <T> List<T> randomSubList1(List<T> allList, int subSize) {
		List<T> sublist = new ArrayList<T>();
		int n = allList.size();
		for (int i = 0; i < subSize; i++) {
			int nextIdx = nextInt(n);
			T ele = allList.get(nextIdx);
			int eIdx = sublist.indexOf(ele);
			while (eIdx >= 0) {
				ele = allList.get(allList.size() - 1 - eIdx);
				eIdx = sublist.indexOf(ele);
			}
			sublist.add(ele);
			n--;
		}
		return sublist;
	}
	
	public static boolean weighedCoinFlip(double trueProb) {
		if (trueProb < 0 || trueProb > 1) {
			throw new IllegalArgumentException("arg must be between 0 and 1.");
		}
		double falseProb = 1 - trueProb;
		return (Randomness.getRandom().nextDouble() >= falseProb);
	}

	public static boolean randomBoolFromDistribution(double trueProb_, double falseProb_) {
		double falseProb = falseProb_ / (falseProb_ + trueProb_);
		return (Randomness.getRandom().nextDouble() >= falseProb);
	}

	public static <T extends HasProbabilityType> T randomWithDistribution(
			T[] eles) {
		if (CollectionUtils.isEmpty(eles)) {
			return null;
		}
		int sum = 0;
		double[] distr = new double[eles.length];
		for (int i = 0; i < eles.length; i++) {
			sum += eles[i].getProb();
			distr[i] = (double) sum;
		}
		for (int i = 0; i < distr.length; i++) {
			distr[i] = distr[i] / sum;
		}
		double randVal = Randomness.getRandom().nextDouble();
		for (int i = 0; i < distr.length; i++) {
			if (randVal < distr[i]) {
				return eles[i];
			}
		}
		return eles[eles.length - 1];
	}

	public static void nextBytes(byte[] bytes) {
		getRandom().nextBytes(bytes);
	}

	public static Double nextDouble() {
		return getRandom().nextDouble();
	}

}
