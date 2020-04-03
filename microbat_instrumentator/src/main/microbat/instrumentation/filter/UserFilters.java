/**
 * 
 */
package microbat.instrumentation.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.LineNumberGen;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

/**
 * @author lyly
 * filter whether an element will be instrumented
 * there are three levels to check:
 * - class
 * - method
 * - instruction
 */
public class UserFilters {
	private List<AbstractUserFilter> filters = new ArrayList<AbstractUserFilter>();
	
	public void register(AbstractUserFilter filter) {
		filters.add(filter);
	}

	public boolean isInstrumentable(String className) {
		for (AbstractUserFilter filter : filters) {
			if (!filter.isInstrumentableClass(className)) {
				return false;
			}
		}
		return true;
	}

	public boolean isInstrumentable(String className, Method method, LineNumberGen[] lineNumbers) {
		for (AbstractUserFilter filter : filters) {
			if (!filter.isInstrumentableMethod(className, method, lineNumbers)) {
				return false;
			}
		}
		return true;
	}

	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		for (AbstractUserFilter filter : filters) {
			filter.filter(lineInsnInfos, className, method);
		}
	}

}
