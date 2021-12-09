package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.MethodFinderByLine;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.Scope;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.sql.IntervalRecorder;
import sav.common.core.utils.CollectionUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * This class manages the tracenodes and queues the node for storage if the threshold is reached
 *
 */
public class HookedTrace extends Trace{
	private final IntervalRecorder recorder;
	private final String traceId;

	public HookedTrace(String traceId, AppJavaClassPath appJavaClassPath, IntervalRecorder recorder) {
		super(appJavaClassPath, traceId);
		this.recorder = recorder;
		this.traceId = traceId;
	}
	
	@Override
	public void addTraceNode(TraceNode node){
		super.addTraceNode(node);
		if (this.getExecutionList().size() > recorder.getThreshold()) {
			this.recorder.partialStore(getThreadName(), getExecutionList());
			setExecutionList(new ArrayList<TraceNode>());
		}
	}
}
