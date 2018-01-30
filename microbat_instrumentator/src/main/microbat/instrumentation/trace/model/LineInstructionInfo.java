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
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;

import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;

public class LineInstructionInfo {
	private LineNumberGen lineGen;
	private List<InstructionHandle> lineInsns;
	private LocalVariableTable localVarTable;
	private ConstantPoolGen constPool;
	
	public LineInstructionInfo(LocalVariableTable localVariableTable, ConstantPoolGen constPool, LineNumberTable lineNumberTable,
			LineNumberGen lineGen, InstructionList insnList) {
		this.lineGen = lineGen;
		this.localVarTable = localVariableTable;
		this.constPool = constPool;
		lineInsns = findCorrespondingInstructions(insnList, lineNumberTable, lineGen.getSourceLine());
	}

	@SuppressWarnings("unchecked")
	public List<RWInstructionInfo> getRWInstructions() {
		List<RWInstructionInfo> rwInsns = new ArrayList<>(Math.min(10, lineInsns.size()));
		for (InstructionHandle insnHandler : lineInsns) {
			Instruction insn = insnHandler.getInstruction();
			if (insn instanceof FieldInstruction) {
				FieldInstruction fieldInsn = (FieldInstruction) insn;
				ReferenceType refType = fieldInsn.getReferenceType(constPool);
				FieldInstructionInfo info = new FieldInstructionInfo(insnHandler, lineGen);
				info.fieldBcType = fieldInsn.getFieldType(constPool);
				info.fieldIndex = getFieldIndex(constPool, fieldInsn);
				info.refType = refType.getSignature();
				info.varType = fieldInsn.getSignature(constPool);
				info.varName = fieldInsn.getFieldName(constPool);
				info.isStore = existIn(insn.getOpcode(), Const.PUTFIELD, Const.PUTSTATIC);
				rwInsns.add(info);
			} else if (insn instanceof ArrayInstruction) {
				ArrayInstructionInfo info = new ArrayInstructionInfo(insnHandler, lineGen);
				info.isStore = CollectionUtils.existIn(insn.getOpcode(), Const.AASTORE, Const.FASTORE, Const.LASTORE,
						Const.CASTORE, Const.IASTORE, Const.BASTORE, Const.SASTORE, Const.DASTORE);
				rwInsns.add(info);
			} else if (insn instanceof LocalVariableInstruction) {
				LocalVariable localVar = localVarTable.getLocalVariable(((LocalVariableInstruction) insn).getIndex(),
						insnHandler.getPosition() + insn.getLength());
				if (localVar == null) {
					throw new SavRtException(String.format("Cannot find localVar with (index = %s, pc = %s)",
							((LocalVariableInstruction) insn).getIndex(), insnHandler.getPosition()));
				}
				LocalVarInstructionInfo info = new LocalVarInstructionInfo(insnHandler, lineGen, localVar.getName(), localVar.getSignature());
				info.isStore = existIn(insn.getOpcode(), Const.FSTORE, Const.IINC, Const.DSTORE, Const.ASTORE,
						Const.ISTORE, Const.LSTORE);
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
	
	private static int getFieldIndex(ConstantPoolGen cpg, FieldInstruction putfield) {
		ObjectType refType = (ObjectType) putfield.getReferenceType(cpg);
		int fieldIndex = FieldIndex.getFieldIndex(refType.getClassName(), putfield.getFieldName(cpg));
		return cpg.addInteger(fieldIndex); // Is now a constant-pool ref
	}
	
	public List<InstructionHandle> getInvokeInstructions() {
		return extractInvokeInstructions(lineInsns);
	}
	
	public LineNumberGen getLineGen() {
		return lineGen;
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
			if ((insn instanceof InvokeInstruction)) {
				invokeInsns.add(insnHandler);
			}
		}
		return invokeInsns;
	}
}
