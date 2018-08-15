package microbat.instrumentation.filter;

import java.util.Collections;
import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.utils.MicrobatUtils;
import sav.common.core.utils.CollectionUtils;

public class InstrumentationFilter {

	public static Set<String> overLongMethods = Collections.emptySet();

	public static IInstrFilter getFilter(String className, Method method, final ConstantPoolGen constPool) {
		if ("java.lang.AbstractStringBuilder".equals(className) 
				&& "append".equals(method.getName())
				&& Type.INT.equals(method.getArgumentTypes()[0])) {
			return new EmptyInstrFilter() {

				@Override
				public boolean isValid(InvokeInstruction instruction, ConstantPoolGen constPool) {
					return !CollectionUtils.existIn(instruction.getMethodName(constPool), "ensureCapacityInternal",
							"getChars", "stringSize");
				}
			};
		}
		String methodFullName = MicrobatUtils.getMicrobatMethodFullName(className, method);
		if (overLongMethods.contains(methodFullName)) {
//			AgentLogger.info("Apply overlongFilter: " + methodFullName);
			return new OverLongFilter();
		}
		return EmptyInstrFilter.getInstance();
	}

}
