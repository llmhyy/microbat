package microbat.instrumentation.trace.bk;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import microbat.instrumentation.trace.model.AccessVariableInfo;

public interface IAccessVariableCollector {

	void collectVariable(CodeIterator iterator, int pos, ConstPool constPool, AccessVariableInfo lineInfo);

}
