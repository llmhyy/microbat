package microbat.instrumentation.filter;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

public class EmptyInstrFilter implements IMethodInstrFilter {
	private static final EmptyInstrFilter instance = new EmptyInstrFilter();
	
	EmptyInstrFilter() {
		
	}
	public static EmptyInstrFilter getInstance() {
		return instance;
	}
	@Override
	public void filter(LineInstructionInfo info) {
		// do nothing
	}

	
}
