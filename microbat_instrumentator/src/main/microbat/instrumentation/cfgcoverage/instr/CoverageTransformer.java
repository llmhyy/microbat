package microbat.instrumentation.cfgcoverage.instr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.cfgcoverage.CoverageAgentParams;

public class CoverageTransformer implements ClassFileTransformer {
	private CoverageInstrumenter instrumenter;
	
	public CoverageTransformer(CoverageAgentParams agentParams) {
		instrumenter = new CoverageInstrumenter(agentParams);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	public CoverageInstrumenter getInstrumenter() {
		return instrumenter;
	}
}
