package microbat.codeanalysis.bytecode;

import org.apache.bcel.classfile.Method;

public abstract class ByteCodeMethodFinder extends ByteCodeVisitor{
	protected Method method;
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
