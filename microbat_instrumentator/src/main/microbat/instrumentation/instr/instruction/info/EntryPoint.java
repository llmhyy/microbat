package microbat.instrumentation.instr.instruction.info;

import sav.common.core.utils.SignatureUtils;

public class EntryPoint {
	private String className;
	private String methodName;
	private String methodSignature;

	public EntryPoint(String className, String method) {
		this.className = className;
		this.methodName = SignatureUtils.extractMethodName(method);
		int endNameIdx = method.indexOf("(");
		if (endNameIdx > 1) {
			methodSignature = method.substring(endNameIdx);
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public boolean matchMethod(String methodName, String methodSignature) {
		return this.methodName.equals(methodName) && 
				(this.methodSignature == null || this.methodSignature.equals(methodSignature));
	}

}
