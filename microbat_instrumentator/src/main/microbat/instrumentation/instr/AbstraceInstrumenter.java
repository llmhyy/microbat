package microbat.instrumentation.instr;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGenException;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public abstract class AbstraceInstrumenter {
	protected static final String CLASS_NAME = "$className"; // local var
	protected static final String METHOD_SIGNATURE = "$methodSignature"; // local var

	public byte[] instrument(String classFName, byte[] classfileBuffer) throws Exception {
		String className = classFName.replace("/", ".");
		ClassParser cp = new ClassParser(new java.io.ByteArrayInputStream(classfileBuffer), classFName);
		JavaClass jc = cp.parse();
		// First, make sure we have to instrument this class:
		if (!jc.isClass()) {
			// could be an interface
			return null;
		}
		
		return instrument(classFName, className, jc);
	}
	
	
	protected abstract byte[] instrument(String classFName, String className, JavaClass jc);

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
	
	protected void appendInstruction(InstructionList insnList, InstructionHandle insnHandler, InstructionList newInsns) {
		updateExceptionTable(insnHandler, insnHandler, newInsns.getEnd());
		insnList.append(insnHandler, newInsns);
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
