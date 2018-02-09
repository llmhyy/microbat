package microbat.instrumentation.trace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.FilterChecker;
import microbat.model.ClassLocation;

public class TraceTransformer implements ClassFileTransformer {
	private TraceInstrumenter instrumenter;
	
	public TraceTransformer(ClassLocation entryPoint) {
		instrumenter = new TraceInstrumenter(entryPoint);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		/* exclude internal classes & libs */
		if (!FilterChecker.isTransformable(classFName) || ExecutionTracer.isShutdown()) {
			return null;
		}
		/* do instrumentation */
		try {
			return instrumenter.instrument(classFName, classfileBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classfileBuffer;
	}

}
