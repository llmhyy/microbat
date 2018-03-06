package microbat.instrumentation.precheck;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.instr.AbstractTransformer;
import microbat.instrumentation.instr.TraceInstrumenter;

public class PrecheckTransformer implements ClassFileTransformer {
	private PrecheckInstrumenter instrumenter;
	
	public PrecheckTransformer(AgentParams params) {
		instrumenter = new PrecheckInstrumenter(params);
	}

	@Override
	public byte[] transform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			if (protectionDomain != null) {
				String path = protectionDomain.getCodeSource().getLocation().getFile();
				if (FilterChecker.isTransformable(classFName, path, false) && FilterChecker.isAppClass(classFName)) {
					byte[] data = instrumenter.instrument(classFName, classfileBuffer);
					AbstractTransformer.log(classfileBuffer, data, classFName, false);
					return data;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getExceedingLimitMethods() {
		return instrumenter.getExceedLimitMethods();
	}

}
