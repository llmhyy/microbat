/**
 * 
 */
package microbat.instrumentation.filter;

import java.util.Iterator;
import java.util.List;

import org.apache.bcel.generic.InstructionHandle;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;

/**
 * @author lyly
 *
 */
public class CodeRangeMethodInstrFilter implements IMethodInstrFilter {
	private List<CodeRangeEntry> list;

	public CodeRangeMethodInstrFilter(List<CodeRangeEntry> list) {
		this.list = list;
	}

	/**
	 * FIXME lyly, as discussed, the whole info should be removed, so that we need to adjust the interface with 
	 * your help :)
	 */
	@Override
	public void filter(LineInstructionInfo info) {
		if(!isLineInRange(info)) {
			Iterator<InstructionHandle> it = info.getInvokeInstructions().iterator();
			while (it.hasNext()) {
				it.remove();
			}			
		}
	}

	private boolean isLineInRange(LineInstructionInfo info) {
		// FIXME XUEZHI
		return false;
	}

}
