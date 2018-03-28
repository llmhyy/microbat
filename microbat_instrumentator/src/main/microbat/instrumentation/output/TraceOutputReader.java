package microbat.instrumentation.output;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.AgentUtils;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.utils.FileUtils;

public class TraceOutputReader extends DataInputStream {
	private String traceExecFolder;
	
	public TraceOutputReader(InputStream in) {
		super(in);
	}
	
	public TraceOutputReader(InputStream in, String traceExecFolder) {
		super(in);
		this.traceExecFolder = traceExecFolder;
	}

	public Trace readTrace() throws IOException {
		int traceNo = readVarInt();
		if (traceNo == 0) {
			return null;
		}
		Trace trace = new Trace(null);
		readString(); // projectName
		readString(); // projectVersion
		readString(); // launchClass
		readString(); // launchMethod
		trace.setMultiThread(readBoolean());
		trace.setIncludedLibraryClasses(readFilterInfo());
		trace.setExcludedLibraryClasses(readFilterInfo());
		List<BreakPoint> locationList = readLocations();
		trace.setExectionList(readSteps(trace, locationList));
		readStepVariableRelation(trace);
		return trace;
	}

	private List<String> readFilterInfo() throws IOException {
		boolean inFile = readBoolean();
		if (inFile) {
			if (traceExecFolder == null) {
				throw new IllegalArgumentException("missing define traceExecFolder!");
			}
			String fileName = readString();
			String filePath = FileUtils.getFilePath(traceExecFolder, fileName);
			return AgentUtils.readLines(filePath);
		} else {
			return readSerializableList();
		}
	}

	private List<BreakPoint> readLocations() throws IOException {
		int bkpTotal = readVarInt();
		int numOfClasses = readVarInt();
		List<BreakPoint> allLocs = new ArrayList<>(bkpTotal);
		for (int i = 0; i < numOfClasses; i++) {
			int lines = readVarInt();
			if (lines <= 0) {
				continue;
			}
			String declaringCompilationUnitName = readString();
			for (int j = 0; j < lines; j++) {
				BreakPoint loc = readLocation(declaringCompilationUnitName);
				allLocs.add(loc);
			}
		}
		return allLocs;
	}

	private List<TraceNode> readSteps(Trace trace, List<BreakPoint> locationList) throws IOException {
		int size = readVarInt();
		List<TraceNode> allSteps = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			TraceNode node = new TraceNode(null, null, i + 1, trace);
			allSteps.add(node);
		}

		for (int i = 0; i < size; i++) {
			TraceNode step = allSteps.get(i);
			step.setBreakPoint(locationList.get(readVarInt()));
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
		return readSerializableList();
	}
	
	@SuppressWarnings("unchecked")
	protected <T>List<T> readSerializableList() throws IOException {
		int size = readVarInt();
		if (size == 0) {
			return new ArrayList<>(0);
		}
		byte[] bytes = readByteArray();
		if (bytes == null || bytes.length == 0) {
			return new ArrayList<>(0);
		}
		List<T> varValues;
		try {
			varValues = (List<T>) ByteConverter.convertFromBytes(bytes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return varValues;
	}

	private TraceNode readNode(List<TraceNode> allSteps) throws IOException {
		int nodeOrder = readVarInt();
		if (nodeOrder == -1) {
			return null;
		}
		return allSteps.get(nodeOrder - 1);
	}

	private BreakPoint readLocation(String declaringCompilationUnitName) throws IOException {
		String classCanonicalName = readString();
		String methodSig = readString();
		int lineNo = readVarInt();
		boolean isConditional = readBoolean();
		boolean isBranch = readBoolean();
		boolean isReturnStatement = readBoolean();
		BreakPoint location = new BreakPoint(classCanonicalName, declaringCompilationUnitName, methodSig, lineNo);
		location.setConditional(isConditional);
		location.setBranch(isBranch);
		location.setReturnStatement(isReturnStatement);
		location.setControlScope(readControlScope());
		location.setLoopScope(readLoopScope());
		return location;
	}

	private ControlScope readControlScope() throws IOException {
		int rangeSize = readVarInt();
		if (rangeSize == 0) {
			return null;
		}
		ControlScope scope = new ControlScope();
		scope.setLoop(readBoolean());
		for (int i = 0; i < rangeSize; i++) {
			ClassLocation controlLoc = new ClassLocation(readString(), null, readVarInt());
			scope.addLocation(controlLoc);
		}
		return scope;
	}

	private SourceScope readLoopScope() throws IOException {
		int size = readVarInt();
		if (size == 0) {
			return null;
		}
		SourceScope scope = new SourceScope();
		scope.setClassName(readString());
		scope.setStartLine(readVarInt());
		scope.setEndLine(readVarInt());
		return scope;
	}
	
	private void readStepVariableRelation(Trace trace) throws IOException {
		Map<String, StepVariableRelationEntry> stepVariableTable = trace.getStepVariableTable();
		int size = readVarInt();
		for (int i = 0; i < size; i++) {
			StepVariableRelationEntry entry = new StepVariableRelationEntry(readString());
			int producerSize = readVarInt();
			for (int p = 0; p < producerSize; p++) {
				entry.addProducer(readNode(trace.getExecutionList()));
				readVarInt();
			}
			int consumerSize = readVarInt();
			for (int p = 0; p < consumerSize; p++) {
				entry.addConsumer(readNode(trace.getExecutionList()));
				readVarInt();
			}
			stepVariableTable.put(entry.getVarID(), entry);
		}
	}

	public String readString() throws IOException {
		int len = readVarInt();
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
	
	public byte[] readByteArray() throws IOException {
		int len = readVarInt();
		if (len == -1) {
			return null;
		} else if (len == 0) {
			return new byte[0];
		} else {
			byte[] bytes = new byte[len];
			readFully(bytes);
			return bytes;
		}
	}
	
	public int readVarInt() throws IOException {
		final int value = 0xFF & readByte();
		if ((value & 0x80) == 0) {
			return value;
		}
		return (value & 0x7F) | (readVarInt() << 7);
	}
}
