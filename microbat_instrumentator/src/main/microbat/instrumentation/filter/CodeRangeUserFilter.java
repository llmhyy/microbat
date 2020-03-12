/**
 * 
 */
package microbat.instrumentation.filter;

import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

/**
 * @author lyly
 *
 */
public class CodeRangeUserFilter extends AbstractUserFilter {
	private List<CodeRangeEntry> list;

	public CodeRangeUserFilter(List<CodeRangeEntry> entries) {
		this.list = entries;
	}

	@Override
	public boolean isInstrumentableClass(String className) {
		// FIXME XUEZHI [3]
		for (CodeRangeEntry entry : list) {
			if (entry.getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isInstrumentableMethod(String className, Method method, LineNumberGen[] lineNumbers) {
		// FIXME XUEZHI [4]
		//foreache method line return false,true.
		for (CodeRangeEntry entry : list) {
			if (entry.getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		// FIXME XUEZHI [5]
		for (LineInstructionInfo lineInfo : lineInsnInfos) {
			if (!isLineInRange(lineInfo, className, method)) {
				Iterator<InstructionHandle> it = lineInfo.getInvokeInstructions().iterator();
				while (it.hasNext()) {
				    //it.next();
					it.remove();
				}
			}
		}

	}

	private boolean isLineInRange(LineInstructionInfo info, String className, Method method) {
		// FIXME XUEZHI [6]
		for (CodeRangeEntry entry : list) {
			if (entry.getClassName().equals(className) && isBetweenStartAndEndLine(info, entry)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBetweenStartAndEndLine(LineInstructionInfo info, CodeRangeEntry entry) {
		boolean result = false;
		int lineNum = info.getLine();
		if (lineNum >= entry.getStartLine() && lineNum <= entry.getEndLine()) {
			result=true;
		}
		return result;

	}
}
