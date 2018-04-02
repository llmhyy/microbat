package microbat.instrumentation.filter;

import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InvokeInstruction;

public class OverLongFilter implements IInstrFilter {

	@Override
	public boolean isValid(FieldInstruction instruction) {
		return true;
	}

	@Override
	public boolean isValid(ArrayInstruction instruction) {
		return false;
	}

	@Override
	public boolean isValid(InvokeInstruction instruction, ConstantPoolGen constPool) {
		return false;
//		String className = instruction.getClassName(constPool);
//		return FilterChecker.isAppClazz(className);
	}

}
