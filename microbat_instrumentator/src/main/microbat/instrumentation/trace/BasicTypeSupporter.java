package microbat.instrumentation.trace;

import org.apache.bcel.Const;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;

public class BasicTypeSupporter {
	
	public int getToPrimitiveValueMethodIdx(BasicType type, ConstantPoolGen cpg) {
		int idx;
		switch (type.getType()) {
		case Const.T_INT:
			idx = cpg.addMethodref("java/lang/Integer", "intValue", "()I");
			break;
		case Const.T_BOOLEAN:
			idx = cpg.addMethodref("java/lang/Boolean", "booleanValue", "()Z");
			break;
		case Const.T_FLOAT:
			idx = cpg.addMethodref("java/lang/Float", "floatValue", "()F");
			break;
		case Const.T_CHAR:
			idx = cpg.addMethodref("java/lang/Character", "charValue", "()C");
			break;
		case Const.T_DOUBLE:
			idx = cpg.addMethodref("java/lang/Double", "doubleValue", "()D");
			break;
		case Const.T_LONG:
			idx = cpg.addMethodref("java/lang/Long", "longValue", "()J");
			break;
		case Const.T_SHORT:
			idx = cpg.addMethodref("java/lang/Short", "shortValue", "()S");
			break;
		case Const.T_BYTE:
			idx = cpg.addMethodref("java/lang/Byte", "byteValue", "()B");
			break;
		default:
			throw new IllegalArgumentException("Unhandled type: " + type);
		}
		return idx;
	}
	
	public int getValueOfMethodIdx(BasicType type, ConstantPoolGen cpg) {
		int idx;
		switch (type.getType()) {
		case Const.T_INT:
			idx = cpg.addMethodref("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
			break;
		case Const.T_BOOLEAN:
			idx = cpg.addMethodref("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
			break;
		case Const.T_FLOAT:
			idx = cpg.addMethodref("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
			break;
		case Const.T_CHAR:
			idx = cpg.addMethodref("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
			break;
		case Const.T_DOUBLE:
			idx = cpg.addMethodref("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
			break;
		case Const.T_LONG:
			idx = cpg.addMethodref("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
			break;
		case Const.T_SHORT:
			idx = cpg.addMethodref("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
			break;
		case Const.T_BYTE:
			idx = cpg.addMethodref("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
			break;
		default:
			throw new IllegalArgumentException("Unhandled type: " + type);
		}
		return idx;
	}
}
