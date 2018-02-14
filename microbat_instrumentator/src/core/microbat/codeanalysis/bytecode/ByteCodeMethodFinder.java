package microbat.codeanalysis.bytecode;

import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public abstract class ByteCodeMethodFinder extends EmptyVisitor{
	protected Method method;
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	protected JavaClass javaClass;
	
	public void setJavaClass(JavaClass clazz) {
		this.javaClass = clazz;
	}

	public JavaClass getJavaClass() {
		return javaClass;
	}
}
