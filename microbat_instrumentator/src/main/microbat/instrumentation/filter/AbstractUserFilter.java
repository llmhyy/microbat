package microbat.instrumentation.filter;

import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.LineNumberGen;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

public abstract class AbstractUserFilter {

	public boolean isInstrumentableClass(String className) {
		return true;
	}

	public boolean isInstrumentableMethod(String className, Method method, LineNumberGen[] lineNumbers) {
		return true;
	}

	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		// do nothing by default
	}

}
