package microbat.instrumentation.instr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import microbat.instrumentation.AgentLogger;

public class SystemClassTransformer {
	
	public static void transformClassLoader(Instrumentation inst) {
		final ClassLoaderInstrumenter classLoaderInstrumenter = new ClassLoaderInstrumenter();
		ClassFileTransformer transformer = new AbstractTransformer() {
			
			@Override
			protected byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
					ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				if ("java/lang/ClassLoader".equals(classFName)) {
					try {
						byte[] data = classLoaderInstrumenter.instrument(classFName, classfileBuffer);
						return data;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
		inst.addTransformer(transformer, true);
		try {
			inst.retransformClasses(ClassLoader.class);
		} catch (UnmodifiableClassException e) {
			AgentLogger.info("Cannot instrument ClassLoader class!");
		}
		inst.removeTransformer(transformer);
	}
}
