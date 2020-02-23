package microbat.instrumentation.filter;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

public interface IMethodInstrFilter {

	void filter(LineInstructionInfo info);

}
