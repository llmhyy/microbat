package microbat.instrumentation.filter;

import java.util.Collections;
import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.utils.MicrobatUtils;

public class InstrumentationFilter {

	public static Set<String> overLongMethods = Collections.emptySet();

	public static IMethodInstrFilter getMethodFilter(String className, Method method, final ConstantPoolGen constPool) {
		IMethodInstrFilter filter = getFilterIfAbstractStringBuilder(className, method);
		if (filter == null) {
			filter = getFilterIfOverLongMethod(className, method);
		}
		if (filter == null) {
			filter = getFilterIfCodeRangeDefined(className, method);
		}
		return EmptyInstrFilter.getInstance();
	}
	
	private static IMethodInstrFilter getFilterIfCodeRangeDefined(String className, Method method) {
		// TODO Xuezhi LinYun
		return null;
	}

	private static IMethodInstrFilter getFilterIfOverLongMethod(String className, Method method) {
		String methodFullName = MicrobatUtils.getMicrobatMethodFullName(className, method);
		if (overLongMethods.contains(methodFullName)) {
//			AgentLogger.info("Apply overlongFilter: " + methodFullName);
			return new OverLongMethodFilter();
		}
		return null;
	}

	private static AbstractStringBuilderMethodFilter getFilterIfAbstractStringBuilder(String className, Method method) {
		if ("java.lang.AbstractStringBuilder".equals(className) 
				&& "append".equals(method.getName())
				&& Type.INT.equals(method.getArgumentTypes()[0])) {
			return new AbstractStringBuilderMethodFilter();
		}
		return null;
	}

}
