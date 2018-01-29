package microbat.instrumentation.trace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import microbat.instrumentation.trace.TraceTransformerChecker.CheckerResult;
import microbat.instrumentation.trace.bk.ITraceInstrumenter;
import microbat.instrumentation.trace.bk.NormalInstrumenter;
import microbat.instrumentation.trace.bk.ReturnInstrumenter;

public class TraceTransformer implements ClassFileTransformer {
	private TraceTransformerChecker checker = new TraceTransformerChecker();
	private NormalInstrumenter normalInstrumenter = new NormalInstrumenter();
	private ReturnInstrumenter returnInstrumenter = new ReturnInstrumenter();
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		/* TODO checking & filter */
		CheckerResult cR = checker.check(className);
		ITraceInstrumenter instrumenter = null;
		switch (cR) {
		case EXCLUDE:
			instrumenter = null;
			break;
		case RETURN_INSTRUMENTATION:
			instrumenter = returnInstrumenter;
			break;
		case NORMAL_INSTRUMENTATION:
			instrumenter = normalInstrumenter;
			break;
		}
		if (instrumenter == null) {
			return classfileBuffer;
		}
		/* do instrumentation */
		try {
			return instrument(className, classfileBuffer, instrumenter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classfileBuffer;
	}

	protected byte[] instrument(String className, byte[] classfileBuffer, ITraceInstrumenter instrumenter) throws Exception {
		CtClass compiledClass;
		ClassPool cp = ClassPool.getDefault();
		compiledClass = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
		CtConstructor staticConstructor = compiledClass.getClassInitializer();
		instrumenter.visitClass(className);
		if (staticConstructor != null) {
			instrumenter.instrument(staticConstructor);
		}
		for (CtMethod method : compiledClass.getDeclaredMethods()) {
			instrumenter.instrument(method);
		}
		return compiledClass.toBytecode();
	}
	
}
