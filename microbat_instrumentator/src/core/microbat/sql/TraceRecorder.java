/**
 * 
 */
package microbat.sql;

import java.util.List;

import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;
import java.util.HashMap;

import microbat.instrumentation.instr.instruction.info.LineInstructionInfo;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;

/**
 * @author knightsong
 *
 */
public interface TraceRecorder {
	void store(List<Trace> trace);
	void serialize(HashMap<Integer, ArrayList<Short>> instructionTable);
}
