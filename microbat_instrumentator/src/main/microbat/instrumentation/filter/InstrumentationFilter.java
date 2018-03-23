package microbat.instrumentation.filter;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

import sav.common.core.utils.CollectionUtils;

public class InstrumentationFilter {

	public static IInstrFilter getFilter(String className, String methodName, Type[] argumentTypes,
			final ConstantPoolGen constPool) {
		if ("java.lang.AbstractStringBuilder".equals(className) 
				&& "append".equals(methodName)
				&& Type.INT.equals(argumentTypes[0])) {
			return new IInstrFilter() {

				@Override
				public boolean isValid(InvokeInstruction instruction) {
					return !CollectionUtils.existIn(instruction.getMethodName(constPool), "ensureCapacityInternal",
							"getChars", "stringSize");
				}
				
			};
		}
		return EmptyInstrFilter.getInstance();
	}

}
