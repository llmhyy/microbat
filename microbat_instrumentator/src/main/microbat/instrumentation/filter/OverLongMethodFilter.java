package microbat.instrumentation.filter;

import java.util.Iterator;

import microbat.instrumentation.instr.instruction.info.ArrayInstructionInfo;
import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import microbat.instrumentation.instr.instruction.info.RWInstructionInfo;

/**
 * 
 * @author lyly
 *
 */
public class OverLongMethodFilter implements IMethodInstrFilter {
	
	@Override
	public void filter(LineInstructionInfo info) {
		Iterator<RWInstructionInfo> it = info.getRWInstructions().iterator();
		while(it.hasNext()) {
			RWInstructionInfo rwInsnInfo = it.next();
			if (rwInsnInfo instanceof ArrayInstructionInfo) {
				it.remove();
			}
		}
		info.getInvokeInstructions().clear();
	}
}
