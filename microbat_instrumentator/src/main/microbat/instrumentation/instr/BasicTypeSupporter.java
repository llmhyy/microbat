package microbat.instrumentation.instr;

import org.apache.bcel.Const;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

public class BasicTypeSupporter {
	
	public void appendObjectConvertInstruction(Type returnType, InstructionList newInsns, ConstantPoolGen constPool) {
		if (!Type.VOID.equals(returnType) && returnType instanceof BasicType) {
			newInsns.append(
					new INVOKESTATIC(getValueOfMethodIdx((BasicType) returnType, constPool)));
		}
	}
	
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

	public ReferenceType getCorrespondingPrimitiveType(BasicType type) {
		switch (type.getType()) {
		case Const.T_INT:
			return ObjectType.getInstance(Integer.class.getName());
		case Const.T_BOOLEAN:
			return ObjectType.getInstance(Boolean.class.getName());
		case Const.T_FLOAT:
			return ObjectType.getInstance(Float.class.getName());
		case Const.T_CHAR:
			return ObjectType.getInstance(Character.class.getName());
		case Const.T_DOUBLE:
			return ObjectType.getInstance(Double.class.getName());
		case Const.T_LONG:
			return ObjectType.getInstance(Long.class.getName());
		case Const.T_SHORT:
			return ObjectType.getInstance(Short.class.getName());
		case Const.T_BYTE:
			return ObjectType.getInstance(Byte.class.getName());
		default:
			throw new IllegalArgumentException("Unhandled type: " + type);
		}
		
	}
}
