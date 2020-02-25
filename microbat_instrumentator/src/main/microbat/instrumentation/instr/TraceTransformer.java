package microbat.instrumentation.instr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.filter.GlobalFilterChecker;

/**
 * 
 * @author lyly
 *
 */
public class TraceTransformer extends AbstractTransformer implements ClassFileTransformer {
	private TraceInstrumenter instrumenter;
	
	public TraceTransformer(AgentParams params) {
		instrumenter = new TraceInstrumenter(params);
	}
	
	@Override
	protected byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		/* bootstrap classes */
		if ((loader == null) || (protectionDomain == null)) {
			if (!GlobalFilterChecker.isTransformable(classFName, null, true)) {
				return null;
			}
		} 
		if (protectionDomain != null) {
			String path = protectionDomain.getCodeSource().getLocation().getFile();
			if (!GlobalFilterChecker.isTransformable(classFName, path, false)) {
				return null;
			}
		}
		
		/* do instrumentation */
		try {
			return instrumenter.instrument(classFName, classfileBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
