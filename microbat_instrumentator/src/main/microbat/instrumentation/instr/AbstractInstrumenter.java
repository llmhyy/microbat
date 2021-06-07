package microbat.instrumentation.instr;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ClassGenException;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.AgentLogger;

public abstract class AbstractInstrumenter {
	protected static final String CLASS_NAME = "$className"; // local var
	protected static final String METHOD_SIGNATURE = "$methodSignature"; // local var
	
	protected BasicTypeSupporter basicTypeSupporter = new BasicTypeSupporter();

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
	
	protected abstract boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen, Method method,
			boolean isAppClass, boolean isMainMethod, boolean isEntry);
	
	
	protected abstract byte[] instrument(String classFName, String className, JavaClass jc);

	protected LocalVariableGen createLocalVariable(String varName, MethodGen methodGen, ConstantPoolGen constPool) {
		InstructionList list = methodGen.getInstructionList();
		LocalVariableGen varGen = methodGen.addLocalVariable(varName, Type.STRING, list.getStart(), list.getEnd());
		return varGen;
	}
	
	protected InstructionHandle insertInsnHandler(InstructionList insnList, InstructionList newInsns,
			InstructionHandle insnHandler) {
		updateTarget(insnHandler, newInsns.getStart(), insnHandler);
		InstructionHandle pos = insnList.insert(insnHandler, newInsns);
		return pos;
	}
	
	protected void appendInstruction(InstructionList insnList, InstructionList newInsns, InstructionHandle insnHandler) {
		updateTarget(insnHandler, insnHandler, newInsns.getEnd());
		insnList.append(insnHandler, newInsns);
	}
	
	protected void updateTarget(InstructionHandle oldPos, InstructionHandle newStart,
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
	
	protected boolean doesBytecodeExceedLimit(MethodGen methodGen) {
		try {
			return methodGen.getInstructionList().getByteCode().length >= 65534;			
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("offset too large")) {
				return true;
			} else {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	protected String[] getArgumentNames(MethodGen methodGen) {
		String methodString = methodGen.toString();
		String args = methodString.substring(methodString.indexOf("(")+1, methodString.indexOf(")"));
		String[] argList = args.split(",");
		for(int i=0; i<argList.length; i++){
			argList[i] = argList[i].trim();
			argList[i] = argList[i].substring(argList[i].indexOf(" ")+1, argList[i].length());
		}
		return argList;
	}
	
	protected LocalVariableGen createMethodParamTypesObjectArrayVar(MethodGen methodGen, ConstantPoolGen constPool,
			InstructionHandle startInsn, InstructionList newInsns, String varName) {
		/* init Object[] */
		LocalVariableGen argObjsVar = methodGen.addLocalVariable(varName, new ArrayType(Type.OBJECT, 1), startInsn,
				startInsn.getNext());
		newInsns.append(new PUSH(constPool, methodGen.getArgumentTypes().length));
		newInsns.append(new ANEWARRAY(constPool.addClass(Object.class.getName())));
		argObjsVar.setStart(newInsns.append(new ASTORE(argObjsVar.getIndex())));
		/* assign method argument values to Object[] */
		LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
		if (localVariableTable != null) {
			int varIdx = (Const.ACC_STATIC & methodGen.getAccessFlags()) != 0 ? 0 : 1;
			for (int i = 0; i < methodGen.getArgumentTypes().length; i++) {
				LocalVariable localVariable = localVariableTable.getLocalVariable(varIdx, 0);
				if (localVariable == null) {
					AgentLogger.debug("Warning: localVariable is empty, varIdx=" + varIdx);
					break;
				}
				newInsns.append(new ALOAD(argObjsVar.getIndex()));
				newInsns.append(new PUSH(constPool, i));
				Type argType = methodGen.getArgumentType(i);
				newInsns.append(InstructionFactory.createLoad(argType, localVariable.getIndex()));
				if (argType instanceof BasicType) {
					newInsns.append(
							new INVOKESTATIC(basicTypeSupporter.getValueOfMethodIdx((BasicType) argType, constPool)));
				}
				newInsns.append(new AASTORE());
				if (Type.DOUBLE.equals(argType) || Type.LONG.equals(argType)) {
					varIdx += 2;
				} else {
					varIdx ++;
				}
			}
		} else {
			AgentLogger.debug("Warning: localVariableTable is empty!");
		}
		return argObjsVar;
	}

}
