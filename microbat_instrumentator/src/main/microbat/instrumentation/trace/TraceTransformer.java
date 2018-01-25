package microbat.instrumentation.trace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import microbat.instrumentation.trace.model.AccessVariableInfo;

public class TraceTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		/* TODO checking & filter */
		/* do instrumentation */
		try {
			return instrument(className, classfileBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classfileBuffer;
	}

	protected byte[] instrument(String className, byte[] classfileBuffer) throws Exception {
		CtClass compiledClass;
		ClassPool cp = ClassPool.getDefault();
		compiledClass = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
		CtConstructor staticConstructor = compiledClass.getClassInitializer();
		if (staticConstructor != null) {
			MethodInfo methodInfo = staticConstructor.getMethodInfo();
			System.out.println(methodInfo.getName());
			CodeAttribute codeAttr = methodInfo.getCodeAttribute();
			CodeIterator iterator = codeAttr.iterator();
			// printer
//			InstructionPrinter printer = new InstructionPrinter(System.out);
			//
			while (iterator.hasNext()) {
				try {
					int pos = iterator.next();
					AccessVariableInfo accessVarInfo = collectVariable(iterator, pos, methodInfo.getConstPool());
//					System.out.println(printer.instructionString(iterator, pos, methodInfo.getConstPool()));
				} catch (BadBytecode e) {
					throw new CannotCompileException(e);
				}
			}
		}
		for (CtMethod method : compiledClass.getDeclaredMethods()) {
			System.out.println(method.getName());
		}
		return compiledClass.toBytecode();
	}

	private AccessVariableInfo collectVariable(CodeIterator iterator, int pos, ConstPool constPool) {
		AccessVariableInfo varInfo = new AccessVariableInfo();
		int opcode = iterator.byteAt(pos);
		switch (opcode) {
		case Opcode.AALOAD:
			
			break;

		default:
			break;
		}
		return varInfo;
	}

}
