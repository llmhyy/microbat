package microbat.instrumentation.filter;

import org.apache.bcel.generic.InvokeInstruction;

public interface IInstrFilter {

	boolean isValid(InvokeInstruction instruction);

}
