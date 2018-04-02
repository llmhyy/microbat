package microbat.instrumentation;

import org.apache.bcel.classfile.Method;

public class ClassGenUtils {

	public static String getMethodFullName(String className, Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append(className).append("#").append(method.getName())
			.append(method.getSignature().replace(";", ":"));
//		.append("(");
//		Type[] argTypes = method.getArgumentTypes();
//		int lastIdx = argTypes.length - 1;
//		for (int i = 0; i < argTypes.length; i++) {
//			sb.append(argTypes[i].getSignature());
//			if (i != lastIdx) {
//				sb.append(";");
//			}
//		}
//		sb.append(")");
//		sb.append(method.getReturnType().getSignature());
		return sb.toString();
	}
	
}
