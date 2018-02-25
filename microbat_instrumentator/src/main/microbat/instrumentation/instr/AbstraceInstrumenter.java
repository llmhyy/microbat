package microbat.instrumentation.instr;

import org.apache.bcel.generic.ClassGenException;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class AbstraceInstrumenter {
	protected static final String CLASS_NAME = "$className"; // local var
	protected static final String METHOD_SIGNATURE = "$methodSignature"; // local var

	protected LocalVariableGen createLocalVariable(String varName, MethodGen methodGen, ConstantPoolGen constPool) {
		InstructionList list = methodGen.getInstructionList();
		LocalVariableGen varGen = methodGen.addLocalVariable(varName, Type.STRING, list.getStart(), list.getEnd());
		return varGen;
	}
	
	protected InstructionHandle insertInsnHandler(InstructionList insnList, InstructionList newInsns,
			InstructionHandle insnHandler) {
		updateExceptionTable(insnHandler, newInsns.getStart(), insnHandler);
		InstructionHandle pos = insnList.insert(insnHandler, newInsns);
		return pos;
	}
	
	protected void updateExceptionTable(InstructionHandle oldPos, InstructionHandle newStart,
			InstructionHandle newEnd) {
		InstructionTargeter[] itList = oldPos.getTargeters();
		if (itList != null) {
			for (InstructionTargeter it : itList) {
				if (it instanceof CodeExceptionGen) {
					CodeExceptionGen exception = (CodeExceptionGen)it;
					if (exception.getStartPC() == oldPos) {
						exception.setStartPC(newStart);
					}
					if (exception.getEndPC() == oldPos) {
						exception.setEndPC(newEnd);
					}
					if (exception.getHandlerPC() == oldPos) {
						exception.setHandlerPC(newStart);
					}
				} else if (it instanceof LocalVariableGen) {
					LocalVariableGen localVarGen = (LocalVariableGen) it;
					boolean targeted = false;
					if (localVarGen.getStart() == oldPos) {
						targeted = true;
						localVarGen.setStart(newStart);
					}
					if (localVarGen.getEnd() == oldPos) {
						targeted = true;
						localVarGen.setEnd(newEnd);
					}
					if (!targeted) {
						throw new ClassGenException("Not targeting " + oldPos + ", but {" + localVarGen.getStart()
								+ ", " + localVarGen.getEnd() + "}");
					}
				} else {
					it.updateTarget(oldPos, newStart);
				}
			}
		}
	}
}
