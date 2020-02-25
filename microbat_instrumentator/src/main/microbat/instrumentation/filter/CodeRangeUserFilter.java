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
		return super.isInstrumentableClass(className);
	}
	
	@Override
	public boolean isInstrumentableMethod(String className, Method method, LineNumberGen[] lineNumbers) {
		// FIXME XUEZHI [4]
		return super.isInstrumentableMethod(className, method, lineNumbers);
	}

	@Override
	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		for (LineInstructionInfo lineInfo : lineInsnInfos) {
			if(!isLineInRange(lineInfo, className, method)) {
				Iterator<InstructionHandle> it = lineInfo.getInvokeInstructions().iterator();
				while (it.hasNext()) {
					it.remove();
				}			
			}
		}
	
	}

	private boolean isLineInRange(LineInstructionInfo info, String className, Method method) {
		// FIXME XUEZHI [5]
		return false;
	}

}
