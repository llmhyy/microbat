package microbat.instrumentation.filter;

import org.apache.bcel.generic.InvokeInstruction;

public class EmptyInstrFilter implements IInstrFilter {
	private static final EmptyInstrFilter instance = new EmptyInstrFilter();
	
	private EmptyInstrFilter() {
		
	}
	public static EmptyInstrFilter getInstance() {
		return instance;
	}

	@Override
	public boolean isValid(InvokeInstruction instruction) {
		return true;
	}
}
