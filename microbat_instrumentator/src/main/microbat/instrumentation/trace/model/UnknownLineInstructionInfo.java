package microbat.instrumentation.trace.model;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;

public class UnknownLineInstructionInfo extends LineInstructionInfo {

	public UnknownLineInstructionInfo(String locId, ConstantPoolGen constPool,
			InstructionList insnList) {
		line = -1;
		lineNumberInsn = insnList.getStart();
		this.constPool = constPool;
		lineInsns = insnList.iterator();
		rwInsructionInfo = extractRWInstructions(locId);
		invokeInsns = extractInvokeInstructions(lineInsns);
		returnInsns = extractReturnInstructions(lineInsns);
	}

}
