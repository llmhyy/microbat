package microbat.instrumentation.filter;

import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InvokeInstruction;

public interface IInstrFilter {

	boolean isValid(InvokeInstruction instruction, ConstantPoolGen constPool);

	boolean isValid(FieldInstruction instruction);

	boolean isValid(ArrayInstruction instruction);

}
