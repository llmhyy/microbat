package microbat.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.IExecutionTracer;

public abstract class AbstractTransformer implements ClassFileTransformer {

	@Override
	public final byte[] transform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (ExecutionTracer.isShutdown()) {
			return null;
		}
		IExecutionTracer tracer = ExecutionTracer.getCurrentThreadStore();
		boolean needToReleaseLock = !tracer.lock();
		
		byte[] data = doTransform(loader, classFName, classBeingRedefined, protectionDomain, classfileBuffer);
		
		if (needToReleaseLock) {
			tracer.unLock();
		}
		return data;
	}

	protected abstract byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;
		
}
