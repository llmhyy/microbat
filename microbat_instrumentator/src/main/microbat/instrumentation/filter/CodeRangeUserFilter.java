/**
 * 
 */
package microbat.instrumentation.filter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.css.ElementCSSInlineStyle;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

/**
 * @author lyly
 *
 */
public class CodeRangeUserFilter extends AbstractUserFilter {
	private List<CodeRangeEntry> list;
	File file = new File("/Users/knightsong/Documents/project/log.txt");

	public CodeRangeUserFilter(List<CodeRangeEntry> entries) {
		this.list = entries;
	}

	@Override
	public boolean isInstrumentableClass(String className) {
		StringBuffer stringBuffer = new StringBuffer(className);
		// FIXME XUEZHI [3]
		for (CodeRangeEntry entry : list) {
			if (entry.getClassName().equals(className)) {
				stringBuffer.append(" true \n");
				return true;
			}
		}
		stringBuffer.append(" fasle \n");
		try {
			FileUtils.writeStringToFile(file, stringBuffer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isInstrumentableMethod(String className, Method method, LineNumberGen[] lineNumbers) {
		StringBuffer stringBuffer = new StringBuffer(className + "." + method.getName());
		// FIXME XUEZHI [4]
		for (CodeRangeEntry entry : list) {
			if (entry.getClassName().equals(className) && isHitMethod(method, entry)) {
				stringBuffer.append(" true \n");
				return true;
			}
		}
		stringBuffer.append(" false \n");
		try {
			FileUtils.writeStringToFile(file, stringBuffer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		// FIXME XUEZHI [5]
		StringBuffer stringBuffer = new StringBuffer(
				className + "." + method.getName() + "total lines num " + lineInsnInfos.size() + "\n");
		int i=0;
		for (LineInstructionInfo lineInfo : lineInsnInfos) {
			++i;
			stringBuffer.append("for no. "+i+" : "); 
			stringBuffer.append("line " + lineInfo.getLine() + " ");
			if (!isLineInRange(lineInfo, className, method)) {
				stringBuffer.append(" is not in range\n");
				Iterator<InstructionHandle> it = lineInfo.getInvokeInstructions().iterator();
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
			}else {
				stringBuffer.append(" is in range\n");
			}
		}
		try {
			FileUtils.writeStringToFile(file, stringBuffer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
