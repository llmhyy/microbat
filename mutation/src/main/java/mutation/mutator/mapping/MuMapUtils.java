/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.mutator.mapping;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.UnaryExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LLT
 *
 */
public class MuMapUtils {
	private MuMapUtils(){}
	private static Logger log = LoggerFactory.getLogger(MuMapUtils.class);
	private static final Map<String, Enum<?>> opMap;
	
	static {
		opMap = new HashMap<String, Enum<?>>();
		opMap.put("==", AssignExpr.Operator.assign);
		opMap.put("+=", AssignExpr.Operator.plus);
		opMap.put("-=", AssignExpr.Operator.minus);
		opMap.put("*=", AssignExpr.Operator.star);
		opMap.put("/=", AssignExpr.Operator.slash);
		opMap.put("&=", AssignExpr.Operator.and);
		opMap.put("|=", AssignExpr.Operator.or);
		opMap.put("^=", AssignExpr.Operator.xor);
		opMap.put("%=", AssignExpr.Operator.rem);
		opMap.put("<<=", AssignExpr.Operator.lShift);
		opMap.put(">>=", AssignExpr.Operator.rSignedShift);
		opMap.put(">>>=", AssignExpr.Operator.rUnsignedShift);
		opMap.put("||", BinaryExpr.Operator.or);
		opMap.put("&&", BinaryExpr.Operator.and);
		opMap.put("|", BinaryExpr.Operator.binOr);
		opMap.put("&", BinaryExpr.Operator.binAnd);
		opMap.put("^", BinaryExpr.Operator.xor);
		opMap.put("==", BinaryExpr.Operator.equals);
		opMap.put("!=", BinaryExpr.Operator.notEquals);
		opMap.put("<", BinaryExpr.Operator.less);
		opMap.put(">", BinaryExpr.Operator.greater);
		opMap.put("<=", BinaryExpr.Operator.lessEquals);
		opMap.put(">=", BinaryExpr.Operator.greaterEquals);
		opMap.put("<<", BinaryExpr.Operator.lShift);
		opMap.put(">>", BinaryExpr.Operator.rSignedShift);
		opMap.put(">>>", BinaryExpr.Operator.rUnsignedShift);
		opMap.put("+", BinaryExpr.Operator.plus);
		opMap.put("-", BinaryExpr.Operator.minus);
		opMap.put("*", BinaryExpr.Operator.times);
		opMap.put("/", BinaryExpr.Operator.divide);
		opMap.put("%", BinaryExpr.Operator.remainder);
		opMap.put("+x", UnaryExpr.Operator.positive);
		opMap.put("-x", UnaryExpr.Operator.negative);
		opMap.put("++x", UnaryExpr.Operator.preIncrement);
		opMap.put("--x", UnaryExpr.Operator.preDecrement);
		opMap.put("!x", UnaryExpr.Operator.not);
		opMap.put("~x", UnaryExpr.Operator.inverse);
		opMap.put("x++", UnaryExpr.Operator.posIncrement);
		opMap.put("x--", UnaryExpr.Operator.posDecrement);
	}
	
	/**
	 * build mutation operator map
	 */
	public static Map<Enum<?>, List<Enum<?>>> buildMuOpMap(
			Map<String, List<String>> configMap) {
		Map<Enum<?>, List<Enum<?>>> muOpMap = new HashMap<Enum<?>, List<Enum<?>>>();
		for (Entry<String, List<String>> entry : configMap.entrySet()) {
			Enum<?> op = getCorrespondingOpEnum(entry.getKey());
			List<Enum<?>> muOps= new ArrayList<Enum<?>>(entry.getValue().size());
			for (String muOpStr : entry.getValue()) {
				muOps.add(getCorrespondingOpEnum(muOpStr));
			}
			if (op != null && !muOps.isEmpty()) {
				muOpMap.put(op, muOps);
			}
		}
		return muOpMap;
	}
	
	private static Enum<?> getCorrespondingOpEnum(String opStr) {
		Enum<?> opEnum = opMap.get(opStr);
		if (opEnum == null) {
			log.warn("Missing define the corresponding Operator Enum for " + opStr);
		}
		return opEnum;
	}
	
}
