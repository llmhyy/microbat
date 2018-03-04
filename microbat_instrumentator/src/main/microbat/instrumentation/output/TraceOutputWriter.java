package microbat.instrumentation.output;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.BreakpointUtils;

public class TraceOutputWriter extends DataOutputStream {
	public static final int READ = 1;
	public static final int WRITE = 2;
	
	public TraceOutputWriter(OutputStream out) {
		super(out);
	}
	
	public void writeTrace(Trace trace) throws IOException {
		int traceNum = (trace == null ? 0 : 1);
		writeVarInt(traceNum);
		writeTrace(trace, null, null, null, null);
	}
	
	public final void writeString(String str) throws IOException {
		if (str == null) {
			writeVarInt(-1);
		} else if ( str.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(str.length());
			writeBytes(str);
		}
	}
	
	public final void writeByteArr(byte[] bytes) throws IOException {
		if (bytes == null) {
			writeVarInt(-1);
		} else if (bytes.length == 0) {
			writeVarInt(0);
		} else {
			writeVarInt(bytes.length);
			write(bytes, 0, bytes.length);
		}
	}
	
	public void writeVarInt(final int value) throws IOException {
		if ((value & 0xFFFFFF80) == 0) {
			writeByte(value);
		} else {
			writeByte(0x80 | (value & 0x7F));
			writeVarInt(value >>> 7);
		}
	}
	
	public void writeTrace(Trace trace, String projectName, String projectVersion, String launchClass,
			String launchMethod) throws IOException {
		writeString(projectName);
		writeString(projectVersion);
		writeString(launchClass);
		writeString(launchMethod);
		writeBoolean(trace.isMultiThread());
		Map<String, Integer> locIdIdxMap = writeLocations(trace);
		writeSteps(trace.getExecutionList(), locIdIdxMap);
		writeStepVariableRelation(trace);
	}
	
	private Map<String, Integer> writeLocations(Trace trace) throws IOException {
		Map<String, Set<BreakPoint>> locationMap = getExecutedLocation(trace);
		writeVarInt(getNumberOfBkps(locationMap)); // number of bkps
		writeVarInt(locationMap.size()); // numberOfClass
		int idx = 0;
		Map<String, Integer> locIdIdxMap = new HashMap<>();
		for (String className : locationMap.keySet()) {
			Set<BreakPoint> bkps = locationMap.get(className);
			writeVarInt(bkps.size()); // lines
			if (bkps.size() <= 0) {
				continue;
			}
			int i = 0;
			for (BreakPoint bkp : bkps) {
				if (i == 0) {
					writeString(bkp.getDeclaringCompilationUnitName()); // DeclaringCompilationUnitName
					i++;
				}
				writeLocation(bkp); // writeLocation
				locIdIdxMap.put(BreakpointUtils.getLocationId(bkp), idx++);
			}
		}
		return locIdIdxMap;
 	}
	
	private int getNumberOfBkps(Map<String, Set<BreakPoint>> locationMap) {
		int size = 0;
		for (Set<BreakPoint> vals : locationMap.values()) {
			size += vals.size();
		}
		return size;
	}

	public Map<String, Set<BreakPoint>> getExecutedLocation(Trace trace){
		Map<String, Set<BreakPoint>> locationMap = new HashMap<>();
		for(TraceNode node: trace.getExecutionList()){
			Set<BreakPoint> bkps = locationMap.get(node.getDeclaringCompilationUnitName());
			if(bkps == null){	
				bkps = new HashSet<>();
				locationMap.put(node.getDeclaringCompilationUnitName(), bkps);
			}
			bkps.add(node.getBreakPoint());
		}
		
		return locationMap;
	}
	
	private void writeSteps(List<TraceNode> exectionList, Map<String, Integer> locIdIdxMap) throws IOException {
		writeVarInt(exectionList.size());
		for (int i = 0; i < exectionList.size(); i++) {
			TraceNode node = exectionList.get(i);
			writeVarInt(locIdIdxMap.get(node.getBreakPoint().getId()));
			writeNodeOrder(node.getControlDominator());
			writeNodeOrder(node.getStepInNext());
			writeNodeOrder(node.getStepOverNext());
			writeNodeOrder(node.getInvocationParent());
			writeNodeOrder(node.getLoopParent());
			writeVarValues(node.getReadVariables());
			writeVarValues(node.getWrittenVariables());
		}
	}

	private void writeVarValues(List<VarValue> readVariables) throws IOException {
		byte[] bytes = ByteConverter.convertToBytes(readVariables);
		writeByteArr(bytes);
	}

	private void writeNodeOrder(TraceNode node) throws IOException {
		if (node != null) {
			writeVarInt(node.getOrder());
		} else {
			writeVarInt(-1);
		}
	}

	private void writeStepVariableRelation(Trace trace) throws IOException {
		writeVarInt(trace.getStepVariableTable().values().size());
		for (StepVariableRelationEntry entry : trace.getStepVariableTable().values()) {
			writeString(entry.getVarID());
			writeVarInt(entry.getProducers().size());
			for (TraceNode node : entry.getProducers()) {
				writeVarInt(node.getOrder());
				writeVarInt(WRITE);
			}
			writeVarInt(entry.getConsumers().size());
			for (TraceNode node : entry.getConsumers()) {
				writeVarInt(node.getOrder());
				writeVarInt(READ);
			}
		}
	}
	
	private void writeLocation(BreakPoint location) throws IOException {
		writeString(location.getClassCanonicalName()); // ClassCanonicalName
		writeString(location.getMethodSign());
		writeVarInt(location.getLineNumber());
		writeBoolean(location.isConditional());
		writeBoolean(location.isBranch());
		writeBoolean(location.isReturnStatement());
		writeConstrolScope(location.getControlScope());
		writeLoopScope(location.getLoopScope());
	}
	
	private void writeConstrolScope(ControlScope controlScope) throws IOException {
		if (controlScope != null && !controlScope.getRangeList().isEmpty()) {
			writeVarInt(controlScope.getRangeList().size());
			writeBoolean(controlScope.isLoop());
			for (ClassLocation controlLoc : controlScope.getRangeList()) {
				writeString(controlLoc.getClassCanonicalName());
				writeVarInt(controlLoc.getLineNumber());
			}
		} else {
			writeVarInt(0);
		}
	}
	
	private void writeLoopScope(SourceScope loopScope) throws IOException {
		if (loopScope == null) {
			writeVarInt(0);
		} else {
			writeVarInt(1);
			writeString(loopScope.getClassName());
			writeVarInt(loopScope.getStartLine());
			writeVarInt(loopScope.getEndLine());
		}
	}
}
