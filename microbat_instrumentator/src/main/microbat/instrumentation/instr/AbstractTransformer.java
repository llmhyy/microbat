package microbat.instrumentation.instr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;
import sav.common.core.utils.FileUtils;

public abstract class AbstractTransformer implements ClassFileTransformer {

	@Override
	public final byte[] transform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (ExecutionTracer.isShutdown()) {
			return null;
		}
		IExecutionTracer tracer = ExecutionTracer.getCurrentThreadStore();
		/*
		 * The reason we need to lock and unlock the tracer:
		 * when a method which is being traced invoke a another method which class is required to be loaded,
		 * we only want to trace inside that invoked method not in class transformer, that's why we lock to 
		 * prevent tracing here.
		 * */
		boolean needToReleaseLock = !tracer.lock();
		if (classFName == null) {
//			AgentLogger.debug("AbstractTransformation-Warning: ClassFName is null");
			return null;
		}
		byte[] data = doTransform(loader, classFName, classBeingRedefined, protectionDomain, classfileBuffer);
		log(classfileBuffer, data, classFName, false);
					
		if (needToReleaseLock) {
			tracer.unLock();
		}
		return data;
	}

	protected abstract byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;
		
	public static void log(byte[] classfileBuffer, byte[] data, String classFName, boolean dump) {
		if (data == null) {
			return;
		}
		String targetFolder = "E:/lyly/Projects";
//		String targetFolder = "/Users/lylytran/Projects";
		if (!dump) {
			AgentLogger.debug("instrumented class: " + classFName);
			return;
		}
		String orgPath = targetFolder + "/inst_src/org/" + classFName.substring(classFName.lastIndexOf(".") + 1)
				+ ".class";
		AgentLogger.debug("dump org class to file: " + orgPath);
		dumpToFile(classfileBuffer, orgPath);
		String filePath = targetFolder + "/inst_src/test/" + classFName.substring(classFName.lastIndexOf(".") + 1)
				+ ".class";
		AgentLogger.debug("dump instrumented class to file: " + filePath);
		dumpToFile(data, filePath);
	}

	private static void dumpToFile(byte[] data, String filePath) {
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
