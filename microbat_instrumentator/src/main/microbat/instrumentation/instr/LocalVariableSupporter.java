package microbat.instrumentation.instr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class LocalVariableSupporter {
	private static final String VAR_ALIAS_NAME_PREFIX = "$lv_";
	
	public static void fillUpVariableTable(MethodGen methodGen, Method method, ConstantPoolGen constPool) {
		int startIdx = (method.isStatic() ? 0 : 1) + method.getArgumentTypes().length;
		LocalVariableTable localVarTable = method.getLocalVariableTable();
		Map<Integer, LocalVariableInfo> localVarMap = new HashMap<>();
		for (InstructionHandle insnHandler : methodGen.getInstructionList()) {
			if (!(insnHandler.getInstruction() instanceof LocalVariableInstruction)) {
				continue;
			}
			LocalVariableInstruction insn = (LocalVariableInstruction) insnHandler.getInstruction();
			int varIdx = insn.getIndex();
			if (varIdx < startIdx) {
				continue;
			}
			if ((localVarTable == null) || (
				localVarTable.getLocalVariable(insn.getIndex(),
						insnHandler.getPosition() + insn.getLength()) == null)) {
				LocalVariableInfo localVarInfo = localVarMap.get(varIdx);
				if (localVarInfo == null) {
					localVarInfo = new LocalVariableInfo(varIdx);
					localVarInfo.type = insn.getType(constPool);
					localVarMap.put(varIdx, localVarInfo);
				}
				localVarInfo.addNewPos(insnHandler);
			}
		}
		if (localVarMap.isEmpty()) {
			return;
		}
		List<LocalVariableInfo> localVariables = new ArrayList<>(localVarMap.values());
		Collections.sort(localVariables, new Comparator<LocalVariableInfo>() {

			@Override
			public int compare(LocalVariableInfo o1, LocalVariableInfo o2) {
				return Integer.compare(o1.index, o2.index);
			}
		});
		int count = 0;
		for (LocalVariableInfo localVar : localVariables) {
			methodGen.addLocalVariable(VAR_ALIAS_NAME_PREFIX + count++, localVar.type, localVar.index, localVar.startPos,
					localVar.endPos);
		}
	}
	
	private static class LocalVariableInfo {
		int index;
		Type type;
		InstructionHandle startPos;
		InstructionHandle endPos;
		
		public LocalVariableInfo(int varIdx) {
			this.index = varIdx;
		}

		public void addNewPos(InstructionHandle insnHandler) {
			if ((startPos == null) || (startPos.getPosition() > insnHandler.getPosition())) {
				startPos = insnHandler;
			}
			
			if ((endPos == null) || (endPos.getPosition() < insnHandler.getPosition())) {
				endPos = insnHandler;
			}
		}
	}
}
