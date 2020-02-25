package microbat.instrumentation.precheck;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.instrumentation.instr.AbstractTransformer;

public class PrecheckTransformer extends AbstractTransformer implements ClassFileTransformer {
	private PrecheckInstrumenter instrumenter;
	private List<String> loadedClasses = new ArrayList<>();
	
	public PrecheckTransformer(AgentParams params) {
		instrumenter = new PrecheckInstrumenter(params);
	}

	@Override
	protected byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			loadedClasses.add(classFName.replace("/", "."));
			if (protectionDomain != null) {
				CodeSource codeSource = protectionDomain.getCodeSource();
				if ((codeSource == null) || (codeSource.getLocation() == null)) {
					AgentLogger.debug(String.format("Transformer- Ignore %s [Code source undefined!]", classFName));
					return null;
				} 
				URL srcLocation = codeSource.getLocation();
				String path = srcLocation.getFile();
				if (!GlobalFilterChecker.isTransformable(classFName, path, false) || !GlobalFilterChecker.isAppClass(classFName)) {
					return null;
				}
				byte[] data = instrumenter.instrument(classFName, classfileBuffer);
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getExceedingLimitMethods() {
		return instrumenter.getExceedLimitMethods();
	}

	public List<String> getLoadedClasses() {
		return loadedClasses;
	}
}
