package microbat.codeanalysis.bytecode;

import org.apache.bcel.classfile.Method;

public class MethodFinderBySignature extends ByteCodeMethodFinder {
	private String signature;

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public MethodFinderBySignature(String signature) {
		super();
		this.signature = signature;
	}

	public void visitMethod(Method method){
		String sig = method.getName() + method.getSignature();
		if(sig.equals(signature)){
			this.setMethod(method);
		}
	}
}
