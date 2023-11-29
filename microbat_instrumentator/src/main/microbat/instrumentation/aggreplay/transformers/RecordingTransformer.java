package microbat.instrumentation.aggreplay.transformers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.bcel.util.ClassPath.ClassFile;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.aggreplay.instrumenter.RecordingInstrumentator;
import microbat.instrumentation.instr.AbstractTransformer;

public class RecordingTransformer extends AbstractTransformer implements ClassFileTransformer {
	private RecordingInstrumentator instrumentator;
	
	public RecordingTransformer(AgentParams params) {
		instrumentator = new RecordingInstrumentator(params);
	}

	@Override
	protected byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		return null;
	}

}
