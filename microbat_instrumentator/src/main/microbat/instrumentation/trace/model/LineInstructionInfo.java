package microbat.instrumentation.trace.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

import sav.common.core.SavRtException;

public class LineInstructionInfo {
	private int line;
	private InstructionHandle lineNumberInsn;
	private List<InstructionHandle> lineInsns;
	private LocalVariableTable localVarTable;
	private ConstantPoolGen constPool;
	private LineNumberTable lineNumberTable;
	private List<RWInstructionInfo> rwInsructionInfo;
	private List<InstructionHandle> invokeInsns;
	private List<InstructionHandle> returnInsns;
	
	public LineInstructionInfo(LocalVariableTable localVariableTable, ConstantPoolGen constPool, LineNumberTable lineNumberTable,
			LineNumberGen lineGen, InstructionList insnList) {
		this.line = lineGen.getSourceLine();
		this.lineNumberInsn = lineGen.getInstruction();
		this.localVarTable = localVariableTable;
		this.constPool = constPool;
		this.lineNumberTable = lineNumberTable;
		lineInsns = findCorrespondingInstructions(insnList, lineNumberTable, lineGen.getSourceLine());
		rwInsructionInfo = extractRWInstructions();
		invokeInsns = extractInvokeInstructions(lineInsns);
		returnInsns = extractReturnInstructions(lineInsns);
	}

	public List<RWInstructionInfo> getRWInstructions() {
		return rwInsructionInfo;
	}

	private List<RWInstructionInfo> extractRWInstructions() {
		List<RWInstructionInfo> rwInsns = new ArrayList<>(Math.min(10, lineInsns.size()));
		for (InstructionHandle insnHandler : lineInsns) {
			Instruction insn = insnHandler.getInstruction();
			if (insn instanceof FieldInstruction) {
				FieldInstruction fieldInsn = (FieldInstruction) insn;
				ReferenceType refType = fieldInsn.getReferenceType(constPool);
				FieldInstructionInfo info = new FieldInstructionInfo(insnHandler, line);
				info.setFieldBcType(fieldInsn.getFieldType(constPool));
				info.setRefType(refType.getSignature());
				info.setVarStackSize(refType.getSize());
				info.setVarType(fieldInsn.getSignature(constPool));
				info.setVarName(fieldInsn.getFieldName(constPool));
				info.setIsStore(existIn(insn.getOpcode(), Const.PUTFIELD, Const.PUTSTATIC));
				rwInsns.add(info);
			} else if (insn instanceof ArrayInstruction) {
				ArrayInstructionInfo info = new ArrayInstructionInfo(insnHandler, line);
				ArrayInstruction arrInsn = (ArrayInstruction) insn;
				Type eleType = arrInsn.getType(constPool);
				info.setElementType(eleType);
				info.setVarType(eleType.getSignature());
				info.setVarStackSize(eleType.getSize());
				info.setIsStore(existIn(insn.getOpcode(), Const.AASTORE, Const.FASTORE, Const.LASTORE,
						Const.CASTORE, Const.IASTORE, Const.BASTORE, Const.SASTORE, Const.DASTORE));
				rwInsns.add(info);
			} else if (insn instanceof LocalVariableInstruction) {
				LocalVariableInstruction localVarInsn = (LocalVariableInstruction) insn;
				LocalVariable localVar = localVarTable.getLocalVariable(localVarInsn.getIndex(),
						insnHandler.getPosition() + insn.getLength());
				if (localVar == null) {
					throw new SavRtException(String.format("Cannot find localVar with (index = %s, pc = %s)",
							localVarInsn.getIndex(), insnHandler.getPosition()));
				}
				LocalVarInstructionInfo info = new LocalVarInstructionInfo(insnHandler, line, localVar.getName(), localVar.getSignature());
				info.setIsStore(existIn(((LocalVariableInstruction) insn).getCanonicalTag(), Const.FSTORE, Const.IINC, Const.DSTORE, Const.ASTORE,
						Const.ISTORE, Const.LSTORE));
				Type type = localVarInsn.getType(constPool);
				info.setVarStackSize(type.getSize());
				info.setVarScopeStartLine(lineNumberTable.getSourceLine(localVar.getStartPC()));
				info.setVarScopeEndLine(lineNumberTable.getSourceLine(localVar.getStartPC() + localVar.getLength()));
				rwInsns.add(info);
			}
		}
		return rwInsns;
	}
	
	private static boolean existIn(short opCode, short... checkOpCodes) {
		for (short checkOpCode : checkOpCodes) {
			if (opCode == checkOpCode) {
				return true;
			}
		}
		return false;
	}
	
	public List<InstructionHandle> getInvokeInstructions() {
		return invokeInsns;
	}
	
	private static List<InstructionHandle> findCorrespondingInstructions(InstructionList list, LineNumberTable lineTable,
			int lineNumber) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();
		Iterator<?> iter = list.iterator();
		while (iter.hasNext()) {
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			int instructionLine = lineTable.getSourceLine(insHandle.getPosition());
			if (instructionLine == lineNumber) {
				correspondingInstructions.add(insHandle);
			}
		}
		return correspondingInstructions;
	}
	
	private static List<InstructionHandle> extractInvokeInstructions(List<InstructionHandle> insnList) {
		List<InstructionHandle> invokeInsns = new ArrayList<>(3);
		for (InstructionHandle insnHandler : insnList) {
			Instruction insn = insnHandler.getInstruction();
			if (insn instanceof InvokeInstruction) {
				invokeInsns.add(insnHandler);
			}
		}
		return invokeInsns;
	}
	
	private List<InstructionHandle> extractReturnInstructions(List<InstructionHandle> insnList) {
		List<InstructionHandle> returnInsns = new ArrayList<>(1);
		for (InstructionHandle insnHandler : insnList) {
			Instruction insn = insnHandler.getInstruction();
			if ((insn instanceof ReturnInstruction)) {
				returnInsns.add(insnHandler);
			}
		}
		return returnInsns;
	}
	
	public List<InstructionHandle> getReturnInsns() {
		return returnInsns;
	}
	
	public int getLine() {
		return line;
	}

	public InstructionHandle getLineNumberInsn() {
		return lineNumberInsn;
	}

	public void dispose() {
		// free memory
		lineInsns = null;
		rwInsructionInfo = null;
		invokeInsns = null;
		returnInsns = null;
	}

	public boolean hasNoInstrumentation() {
		return rwInsructionInfo.isEmpty() && invokeInsns.isEmpty() && returnInsns.isEmpty();
	}
}
