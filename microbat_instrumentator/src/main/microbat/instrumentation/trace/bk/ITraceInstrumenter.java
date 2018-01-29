package microbat.instrumentation.trace.bk;

import javassist.CtBehavior;

public interface ITraceInstrumenter {

	void instrument(CtBehavior method) throws Exception;

	void visitClass(String className);
	
}
