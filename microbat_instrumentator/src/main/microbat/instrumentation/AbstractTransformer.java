package microbat.instrumentation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.IExecutionTracer;
import sav.common.core.utils.FileUtils;

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
		log(data, classFName, false);
					
		if (needToReleaseLock) {
			tracer.unLock();
		}
		return data;
	}

	protected abstract byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;
		
	private void log(byte[] data, String className, boolean dump) {
		if (!dump) {
			System.out.println("instrumented class: " + className);
			return;
		}
		String filePath = "E:/lyly/Projects/inst_src/test/" + className.substring(className.lastIndexOf(".") + 1)
				+ ".class";
		System.out.println("dump instrumented class to file: " + filePath);
		FileUtils.getFileCreateIfNotExist(filePath);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			out.write(data);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
