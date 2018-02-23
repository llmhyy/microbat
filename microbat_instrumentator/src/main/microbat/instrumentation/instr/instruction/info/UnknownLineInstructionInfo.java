package microbat.instrumentation.instr.instruction.info;

import java.util.Arrays;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;

public class UnknownLineInstructionInfo extends LineInstructionInfo {

	public UnknownLineInstructionInfo(String locId, ConstantPoolGen constPool,
			InstructionList insnList, boolean isAppClass) {
		line = -1;
		lineNumberInsn = insnList.getStart();
		this.constPool = constPool;
		lineInsns = Arrays.asList(insnList.getInstructionHandles());
		rwInsructionInfo = extractRWInstructions(locId, isAppClass);
		invokeInsns = extractInvokeInstructions(lineInsns);
		returnInsns = extractReturnInstructions(lineInsns);
	}

}
