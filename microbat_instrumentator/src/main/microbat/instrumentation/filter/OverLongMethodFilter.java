package microbat.instrumentation.filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.Method;

import microbat.instrumentation.instr.instruction.info.ArrayInstructionInfo;
import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import microbat.instrumentation.instr.instruction.info.RWInstructionInfo;
import microbat.instrumentation.utils.MicrobatUtils;

/**
 * 
 * @author lyly
 *
 */
public class OverLongMethodFilter extends AbstractUserFilter {
	private Set<String> overLongMethods = Collections.emptySet();
	
	public OverLongMethodFilter(Set<String> overLongMethods) {
		this.overLongMethods = overLongMethods;
	}

	@Override
	public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
		String methodFullName = MicrobatUtils.getMicrobatMethodFullName(className, method);
		if (overLongMethods.contains(methodFullName)) {
			for (LineInstructionInfo lineInfo : lineInsnInfos) {
				Iterator<RWInstructionInfo> it = lineInfo.getRWInstructions().iterator();
				while(it.hasNext()) {
					RWInstructionInfo rwInsnInfo = it.next();
					if (rwInsnInfo instanceof ArrayInstructionInfo) {
						it.remove();
					}
				}
				lineInfo.getInvokeInstructions().clear();
			}
		}
	}
}
