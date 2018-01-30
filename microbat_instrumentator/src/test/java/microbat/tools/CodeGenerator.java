package microbat.tools;

import java.lang.reflect.Method;

import org.junit.Test;

import microbat.instrumentation.trace.data.ExecutionTracer;
import sav.common.core.utils.SignatureUtils;

public class CodeGenerator {

	@Test
	public void generateCode_ExecutionTracerIdx() {
		for (Method method : ExecutionTracer.class.getDeclaredMethods()) {
			String signature = SignatureUtils.getSignature(method);
			String format = "executionTracer_%s_idx = cpg.addMethodref(\"%s\", \"%s\", \"%s\");";
			System.out.println(String.format(format, method.getName(),ExecutionTracer.class.getName().replace(".", "/"),
					method.getName(), signature));
		}
	}
	
}
