package microbat.instrumentation.cfgcoverage.graph;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Const;

public enum BranchCondition {
	EQ, GT_GE, LT_LE, NEQ;
	
	private static Map<Short, BranchCondition> condMap;
	private static Map<BranchCondition, BranchCondition> negationMap;
	
	static {
		condMap = new HashMap<>();
		condMap.put(Const.IF_ACMPEQ, EQ);
		condMap.put(Const.IF_ACMPNE, NEQ);
		condMap.put(Const.IF_ICMPEQ, EQ);
		condMap.put(Const.IF_ICMPNE, NEQ);
		condMap.put(Const.IF_ICMPLT, LT_LE);
		condMap.put(Const.IF_ICMPGE, GT_GE);
		condMap.put(Const.IF_ICMPGT, GT_GE);
		condMap.put(Const.IF_ICMPLE, LT_LE);
		condMap.put(Const.IFEQ, EQ);
		condMap.put(Const.IFNE, NEQ);
		condMap.put(Const.IFLT, LT_LE);
		condMap.put(Const.IFGE, GT_GE);
		condMap.put(Const.IFGT, GT_GE);
		condMap.put(Const.IFLE, LT_LE);
		condMap.put(Const.IFNONNULL, NEQ);
		condMap.put(Const.IFNULL, EQ);
		condMap.put(Const.IF_ACMPEQ, EQ);
		condMap.put(Const.IF_ACMPEQ, EQ);
		condMap.put(Const.IF_ACMPEQ, EQ);
		condMap.put(Const.IF_ACMPEQ, EQ);
		condMap.put(Const.DCMPG, GT_GE);
		condMap.put(Const.DCMPL, LT_LE);
		condMap.put(Const.FCMPG, GT_GE);
		condMap.put(Const.FCMPL, GT_GE);
		condMap.put(Const.LCMP, GT_GE);
		
		negationMap = new HashMap<>();
		negationMap.put(EQ, NEQ);
		negationMap.put(BranchCondition.NEQ, EQ);
		negationMap.put(BranchCondition.GT_GE, LT_LE);
		negationMap.put(LT_LE, GT_GE);
	}

	public static BranchCondition valueOf(short ifOpcode) {
		BranchCondition cond = condMap.get(ifOpcode);
		if (cond == null) {
			throw new IllegalArgumentException("Missing handle for the case of opcode=" + ifOpcode);
		}
		return cond;
	}

	public static BranchCondition valueOf(short notIntCmpOpcode, short ifOpcode) {
		BranchCondition notIntCmpCond = condMap.get(notIntCmpOpcode);
		BranchCondition ifCond = condMap.get(ifOpcode);
		switch (ifCond) {
		case GT_GE:
			return notIntCmpCond;
		case LT_LE:
			return negate(notIntCmpCond);
		case EQ:
			return EQ;
		case NEQ:
			return NEQ;
		}
		throw new IllegalArgumentException(String
				.format("Missing handle for the case of notIntCmpOpcode=%s, ifOpcode=%s", notIntCmpCond, ifOpcode));
	}

	public static BranchCondition negate(BranchCondition cond) {
		return negationMap.get(cond);
	}
}
