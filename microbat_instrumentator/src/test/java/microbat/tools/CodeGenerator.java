package microbat.tools;

import java.lang.reflect.Method;

import javax.print.attribute.standard.Chromaticity;

import org.junit.Test;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.IExecutionTracer;
import sav.common.core.utils.SignatureUtils;

public class CodeGenerator {

	@Test
	public void generateCode_ExecutionTracerIdx() {
		Method[] methods = ExecutionTracer.class.getMethods();
		for (Method method : methods) {
			if (!method.getName().startsWith("_")) {
				continue;
			}
			String className = IExecutionTracer.class.getName();
			if ("_getTracer".equals(method.getName())) {
				className = ExecutionTracer.class.getName();
			}
			String signature = SignatureUtils.getSignature(method);
			String format = "executionTracer%s_idx = cpg.addMethodref(\"%s\", \"%s\", \"%s\");";
			System.out.println(String.format(format, method.getName(), className.replace(".", "/"),
					method.getName(), signature));
		}
		
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!method.getName().startsWith("_")) {
				continue;
			}
			String className = IExecutionTracer.class.getName();
			if ("_getTracer".equals(method.getName())) {
				className = ExecutionTracer.class.getName();
			}
			String format = "%s(\"%s\", \"%s\", \"%s\", %d),";
			String methodName = method.getName();
			char[] charArray = methodName.toCharArray();
			StringBuilder sb = new StringBuilder();
			for (int j = 1; j < charArray.length; j++) {
				char ch = charArray[j];
				if (Character.isUpperCase(ch)) {
					sb.append("_").append(ch);
				} else {
					sb.append(ch);
				}
			}
			String signature = SignatureUtils.getSignature(method);
			System.out.println(String.format(format, sb.toString().toUpperCase(),
					className,
					method.getName(),
					signature,
					method.getParameters().length + 1));
		}
	}
	
}
