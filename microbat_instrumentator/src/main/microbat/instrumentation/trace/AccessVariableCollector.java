package microbat.instrumentation.trace;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.Opcode;
import microbat.instrumentation.trace.model.AccessVariableInfo;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.util.PrimitiveUtils;

public class AccessVariableCollector implements IAccessVariableCollector {

	public void collectVariable(CodeIterator iterator, int pos, ConstPool constPool, AccessVariableInfo lineInfo) {
		lineInfo.addPc(pos);
		InstructionPrinter printer = new InstructionPrinter(System.out);
		System.out.println(printer.instructionString(iterator, pos, constPool));
		int opcode = iterator.byteAt(pos);
		switch (opcode) {
		case Opcode.AALOAD:
			
			break;
		case Opcode.IASTORE:
			lineInfo.addWrittenVar(localVarInfo(opcode, constPool, pos));
		case Opcode.LASTORE:
			
		case Opcode.PUTSTATIC:
			lineInfo.addWrittenVar(fieldInfo(constPool, iterator.u16bitAt(pos + 1), true));
			break;
		case Opcode.GETSTATIC:
			lineInfo.addReadVar(fieldInfo(constPool, iterator.u16bitAt(pos + 1), true));
			break;
		case Opcode.PUTFIELD:
			
			break;
		case Opcode.INVOKESTATIC:
		case Opcode.INVOKEVIRTUAL:
			// TODO-LLT: handle for case in which return type is not void.
		default:
			break;
		}
	}
	
	
	private LocalVar localVarInfo(int opcode, ConstPool constPool, int pos) {
		String type = getType(opcode);
		String name = null;
		String locationClass = null;
		int lineNumber = 0;
		LocalVar var = new LocalVar(name, type, locationClass, lineNumber);
		return var;
	}

    private FieldVar fieldInfo(ConstPool pool, int index, boolean isStatic) {
    	FieldVar var = new FieldVar(isStatic, pool.getFieldrefName(index), pool.getFieldrefType(index));
    	var.setDeclaringType(pool.getFieldrefClassName(index));
    	return var;
    }

    private String getType(int opcode) {
    	switch (opcode) {
    	case Opcode.IASTORE:
    	case Opcode.IALOAD:
    		return PrimitiveUtils.T_INT;
    	default:
    		break;
    	}
    	throw new IllegalArgumentException("Unhandled opcode: " + opcode);
    }
}
