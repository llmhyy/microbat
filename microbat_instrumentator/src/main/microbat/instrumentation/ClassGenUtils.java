package microbat.instrumentation;

import org.apache.bcel.classfile.Method;

public class ClassGenUtils {

	public static String getMethodFullName(String className, Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append(className).append("#").append(method.getName())
			.append(method.getSignature().replace(";", ":"));
		return sb.toString();
	}
	
}
