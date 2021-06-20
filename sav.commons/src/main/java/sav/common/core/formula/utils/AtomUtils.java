/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.formula.utils;

import java.util.Comparator;
import static sav.common.core.formula.Operator.*;
import sav.common.core.Pair;
import sav.common.core.formula.Atom;
import sav.common.core.formula.LIAAtom;
import sav.common.core.formula.LIATerm;
import sav.common.core.formula.Operator;

/**
 * @author LLT
 * 
 */
public class AtomUtils {
	public static final int ATOMS_NO_RELATIONSHIP = 0;
	public static final int ATOMS_EQUAL = 1;
	public static final int ATOMS_A_BOUND_B = 2;
	public static final int ATOMS_B_BOUND_A = 3;
	public static final int ATOMS_NEGATION = 4;
	
	public static int getRelationship(Atom a, Atom b) {
		if (a == b) {
			return ATOMS_EQUAL;
		}
		if (!(a instanceof LIAAtom) || !(b instanceof LIAAtom)) {
			return ATOMS_NO_RELATIONSHIP;
		}
		LIATerm curTerm = ((LIAAtom)a).getSingleTerm();
		LIATerm liaTerm = ((LIAAtom)b).getSingleTerm();
		if (curTerm == null || liaTerm == null) {
			return ATOMS_NO_RELATIONSHIP;
		}
		return getRelationship((LIAAtom)a, (LIAAtom)b);
	}

	public static int getRelationship(LIAAtom curAtom, LIAAtom liaAtom) {
		if (liaAtom.equals(curAtom)) {
			return ATOMS_EQUAL;
		}
		LIATerm curTerm = curAtom.getSingleTerm();
		LIATerm liaTerm = liaAtom.getSingleTerm();
		if (curTerm != null
				&& curTerm.getVariable().equals(liaTerm.getVariable())) {
			Pair<Operator, Operator> opPair = Pair.of(
					curAtom.getOperator().negateIfPlusNegValue(
							curTerm.getCoefficient()),
					liaAtom.getOperator().negateIfPlusNegValue(
							liaTerm.getCoefficient()));
			if (opPair.equals(Pair.of(EQ, EQ))) {
				return ATOMS_NEGATION;
			}
			double curConst = curAtom.getConstant() / curTerm.getCoefficient();
			double liaConst = liaAtom.getConstant() / liaTerm.getCoefficient();
			if (isNegation(opPair.first(), opPair.second(), curConst, liaConst)
					|| isHalfNegation(opPair.first(), opPair.second(), curConst, liaConst)
					|| isHalfNegation(opPair.second(), opPair.first(), liaConst, curConst)) {
				return ATOMS_NEGATION;
			}
			if (isBound(opPair.first(), opPair.second(), curConst, liaConst)) {
				return ATOMS_A_BOUND_B;
			}
			if (isBound(opPair.first(), opPair.second(), liaConst, curConst)) {
				return ATOMS_B_BOUND_A;
			}
		}
		return ATOMS_NO_RELATIONSHIP;
	}

	/**
	 * A = x (>, >=) a &&  B = x (>, >=) b, a > b
	 * A = x (<, <=) a &&  B = x (<, <=) b, a < b
	 * => A bounds B
	 * 
	 */
	private static boolean isBound(Operator a, Operator b, double aConst,
			double bConst) {
		return (a.isGT() && b.isGT() && aConst > bConst)
				|| (a.isLT() && b.isLT() && aConst < bConst);
	}

	private static boolean isNegation(Operator a, Operator b, double aConst,
			double bConst) {
		return (aConst == bConst) && (a.negative() == b);
	}

	private static boolean isHalfNegation(Operator a, Operator b,
			double aConst, double bConst) {
		if (a.isGT() && b.isLT() && (aConst > bConst)) {
			return true;
		}
		return false;
	}

	public static Comparator<? super Atom> getComparator() {
		return new Comparator<Atom>() {

			@Override
			public int compare(Atom o1, Atom o2) {
				int relationship = getRelationship(o1, o2);
				if (relationship == ATOMS_A_BOUND_B) {
					return -1;
				}
				if (relationship == ATOMS_B_BOUND_A) {
					return 1;
				}
				return 0;
			}
		};
	}
}
