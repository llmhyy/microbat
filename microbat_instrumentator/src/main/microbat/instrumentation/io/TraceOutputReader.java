package microbat.instrumentation.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.handler.xml.VarValueXmlReader;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class TraceOutputReader extends DataInputStream {

	public TraceOutputReader(InputStream in) {
		super(in);
	}

	public Trace readTrace() throws IOException {
		int traceNo = readInt();
		if (traceNo == 0) {
			return null;
		}
		Trace trace = new Trace(null);
		readString(); // projectName
		readString(); // projectVersion
		readString(); // launchClass
		readString(); // launchMethod
		trace.setMultiThread(readBoolean());
		trace.setExectionList(readSteps(trace));
		readStepVariableRelation(trace);
		return trace;
	}

	private List<TraceNode> readSteps(Trace trace) throws IOException {
		int size = readInt();
		List<TraceNode> allSteps = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			TraceNode node = new TraceNode(null, null, i + 1, trace);
			allSteps.add(node);
		}

		for (int i = 0; i < size; i++) {
			TraceNode step = allSteps.get(i);
			step.setBreakPoint(readLocation());
			TraceNode controlDominator = readNode(allSteps);
			step.setControlDominator(controlDominator);
			if (controlDominator != null) {
				controlDominator.addControlDominatee(step);
			}
			// step_in
			TraceNode stepIn = readNode(allSteps);
			step.setStepInNext(stepIn);
			if (stepIn != null) {
				stepIn.setStepInPrevious(step);
			}
			// step_over
			TraceNode stepOver = readNode(allSteps);
			step.setStepOverNext(stepOver);
			if (stepOver != null) {
				stepOver.setStepOverPrevious(step);
			}
			// invocation_parent
			TraceNode invocationParent = readNode(allSteps);
			step.setInvocationParent(invocationParent);
			if (invocationParent != null) {
				invocationParent.addInvocationChild(step);
			}
			// loop_parent
			TraceNode loopParent = readNode(allSteps);
			step.setLoopParent(loopParent);
			if (loopParent != null) {
				loopParent.addLoopChild(step);
			}
			step.setReadVariables(readVarValue());
			step.setWrittenVariables(readVarValue());
		}
		return allSteps;
	}

	protected List<VarValue> readVarValue() throws IOException {
		// xmlContent = xmlContent.replace("&#", "#");
		return VarValueXmlReader.read(readString());
	}

	private TraceNode readNode(List<TraceNode> allSteps) throws IOException {
		int nodeOrder = readInt();
		if (nodeOrder == -1) {
			return null;
		}
		return allSteps.get(nodeOrder - 1);
	}

	private BreakPoint readLocation() throws IOException {
		String className = readString();
		String declaringCompilationUnitName = readString();
		String methodSig = readString();
		int lineNo = readInt();
		boolean isConditional = readBoolean();
		boolean isReturnStatement = readBoolean();
		BreakPoint location = new BreakPoint(className, declaringCompilationUnitName, methodSig, lineNo);
		location.setConditional(isConditional);
		location.setReturnStatement(isReturnStatement);
		location.setControlScope(readControlScope());
		location.setLoopScope(readLoopScope());
		return location;
	}

	private ControlScope readControlScope() throws IOException {
		int rangeSize = readInt();
		if (rangeSize == 0) {
			return null;
		}
		ControlScope scope = new ControlScope();
		scope.setLoop(readBoolean());
		for (int i = 0; i < rangeSize; i++) {
			ClassLocation controlLoc = new ClassLocation(readString(), null, readInt());
			scope.addLocation(controlLoc);
		}
		return null;
	}

	private SourceScope readLoopScope() throws IOException {
		int size = readInt();
		if (size == 0) {
			return null;
		}
		SourceScope scope = new SourceScope();
		scope.setClassName(readString());
		scope.setStartLine(readInt());
		scope.setEndLine(readInt());
		return scope;
	}
	
	private void readStepVariableRelation(Trace trace) throws IOException {
		Map<String, StepVariableRelationEntry> stepVariableTable = trace.getStepVariableTable();
		int size = readInt();
		for (int i = 0; i < size; i++) {
			StepVariableRelationEntry entry = new StepVariableRelationEntry(readString());
			int producerSize = readInt();
			for (int p = 0; p < producerSize; p++) {
				entry.addProducer(readNode(trace.getExecutionList()));
				readInt();
			}
			int consumerSize = readInt();
			for (int p = 0; p < consumerSize; p++) {
				entry.addConsumer(readNode(trace.getExecutionList()));
				readInt();
			}
			stepVariableTable.put(entry.getVarID(), entry);
		}
	}

	public String readString() throws IOException {
		int len = readInt();
		if (len == -1) {
			return null;
		} else if (len == 0) {
			return "";
		} else {
			byte[] bytes = new byte[len];
			readFully(bytes);
			return new String(bytes);
		}
	}
}
