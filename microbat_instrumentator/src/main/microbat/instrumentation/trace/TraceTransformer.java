package microbat.instrumentation.trace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.FilterChecker;

public class TraceTransformer implements ClassFileTransformer {
	private TraceInstrumenter instrumenter;
	
	public TraceTransformer() {
		instrumenter = new TraceInstrumenter();
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		/* exclude internal classes & libs */
		if (!FilterChecker.isTransformable(className) || ExecutionTracer.isShutdown()) {
			return null;
		}
		/* do instrumentation */
		try {
			return instrumenter.instrument(className, classfileBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classfileBuffer;
	}

}
