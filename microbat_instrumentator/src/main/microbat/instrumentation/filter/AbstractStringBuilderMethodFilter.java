package microbat.instrumentation.filter;

import java.util.Iterator;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import sav.common.core.utils.CollectionUtils;

/**
 * 
 * @author lyly
 *
 */
public class AbstractStringBuilderMethodFilter implements IMethodInstrFilter {

	@Override
	public void filter(LineInstructionInfo info) {
		Iterator<InstructionHandle> it = info.getInvokeInstructions().iterator();
		while (it.hasNext()) {
			String methodName = ((InvokeInstruction) it.next().getInstruction()).getMethodName(info.getConstPool());
			if (CollectionUtils.existIn(methodName, "ensureCapacityInternal",
					"getChars", "stringSize")) {
				it.remove();
			}
		}
	}

}
