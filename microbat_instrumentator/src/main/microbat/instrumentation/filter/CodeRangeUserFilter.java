/**
 * 
 */
package microbat.instrumentation.filter;

import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
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
		for (CodeRangeEntry entry : list) {
			if (entry.getClassName().equals(className) && isHitMethod(method, entry)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		// FIXME XUEZHI [5]
		Iterator<LineInstructionInfo> iter=lineInsnInfos.iterator();
		while (iter.hasNext()) {
			LineInstructionInfo lineInfo = iter.next(); 
			if (!isLineInRange(lineInfo, className, method)) {
				iter.remove();		
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
			result = true;
		}
		return result;

	}

	// whether codeRange hits the method
	private boolean isHitMethod(Method method, CodeRangeEntry entry) {
		boolean result = false;
		LineNumber[] lineNumbers = method.getLineNumberTable().getLineNumberTable();

		// CodeRange hits method body means the two sets overlap
		int mbsl = lineNumbers[0].getLineNumber(); // methodBodyStartLine
		int mbel = lineNumbers[lineNumbers.length - 1].getLineNumber(); // methodBodyEndLine
		int crsl = entry.getStartLine(); // codeRangeStartLine
		int crel = entry.getEndLine(); // codeRangeEndLine

		if (Math.max(mbsl, crsl) <= Math.min(mbel, crel)) {
			result = true;
		}
		return result;
	}
}
