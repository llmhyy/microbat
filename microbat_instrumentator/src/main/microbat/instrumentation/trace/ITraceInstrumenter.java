package microbat.instrumentation.trace;

import javassist.CtBehavior;

public interface ITraceInstrumenter {

	void instrument(CtBehavior method) throws Exception;

	void visitClass(String className);
	
}
