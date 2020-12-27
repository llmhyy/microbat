package microbat.instrumentation.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
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
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.StringUtils;

public class TraceOutputWriter extends OutputWriter {
	public static final int READ = 1;
	public static final int WRITE = 2;
	private String traceExecFolder;
	private String filterFilePrefix;
	
	public TraceOutputWriter(OutputStream out) {
		super(out);
	}
	
	public TraceOutputWriter(OutputStream out, String traceExecFolder, String filterFilePrefix) {
		super(out);
		this.traceExecFolder = traceExecFolder;
		this.filterFilePrefix = filterFilePrefix;
	}
	
	public void writeTrace(List<Trace> traceList) throws IOException {
		int traceNum = traceList.size();
		writeVarInt(traceNum);
		for(Trace trace: traceList) {
			writeTrace(trace, null, null, null, null);			
		}
	}
	
	public void writeTrace(Trace trace, String projectName, String projectVersion, String launchClass,
			String launchMethod) throws IOException {
		writeString(projectName);
		writeString(projectVersion);
		writeString(launchClass);
		writeString(launchMethod);
		writeBoolean(trace.isMain());
		writeString(trace.getThreadName());
		writeString(String.valueOf(trace.getThreadId()));
		writeFilterInfo(trace.getIncludedLibraryClasses(), true);
		writeFilterInfo(trace.getExcludedLibraryClasses(), false);
		Map<String, Integer> locIdIdxMap = writeLocations(trace);
		writeSteps(trace.getExecutionList(), locIdIdxMap);
		writeStepVariableRelation(trace);
	}
	
	private void writeFilterInfo(List<String> libClasses, boolean isInclusive) throws IOException {
		if (libClasses.size() > 300 && (traceExecFolder != null)) {
			writeBoolean(true); // write file
			String fileName = filterFilePrefix + (isInclusive ? "_includes.info" : "_excludes.info");
			String filePath = FileUtils.getFilePath(traceExecFolder, fileName);
			FileUtils.writeFile(filePath, StringUtils.newLineJoin(libClasses));
			writeString(fileName);
		} else {
			writeBoolean(false);
			writeSerializableList(libClasses);
		}
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
	
	private void writeSteps(List<TraceNode> exectionList, Map<String, Integer> locIdIdxMap)
			throws IOException {
		writeVarInt(exectionList.size());
		List<Collection<VarValue>> allReadVars = new ArrayList<>(exectionList.size());
		List<Collection<VarValue>> allWrittenVars = new ArrayList<>(exectionList.size());
		for (int i = 0; i < exectionList.size(); i++) {
			TraceNode node = exectionList.get(i);
			writeVarInt(locIdIdxMap.get(node.getBreakPoint().getId()));
			writeLong(node.getTimestamp());
			writeNodeOrder(node.getControlDominator());
			writeNodeOrder(node.getStepInNext());
			writeNodeOrder(node.getStepOverNext());
			writeNodeOrder(node.getInvocationParent());
			writeNodeOrder(node.getLoopParent());
			allReadVars.add(node.getReadVariables());
			allWrittenVars.add(node.getWrittenVariables());
			writeBoolean(node.isException());
		}
		writeVarValues(allReadVars);
		writeVarValues(allWrittenVars);
	}
	
	private void writeVarValues(List<Collection<VarValue>> list) throws IOException {
		int idx = 0;
		while (idx < list.size()) {
			int limitSize = 0;
			List<Collection<VarValue>> subList = new ArrayList<>();
			while (limitSize < 4000 && (idx < list.size())) {
				Collection<VarValue> vars = list.get(idx++);
				subList.add(vars);
				limitSize = subList.size();
			}
			if (subList == null || subList.isEmpty()) {
				writeVarInt(0);
			} else {
				writeVarInt(subList.size());
				byte[] bytes = ByteConverter.convertToBytes(subList);
				writeByteArr(bytes);
			}
		}
	}
	
	private void writeNodeOrder(TraceNode node) throws IOException {
		if (node != null) {
			writeVarInt(node.getOrder());
		} else {
			writeVarInt(-1);
		}
	}

	private void writeStepVariableRelation(Trace trace) throws IOException {
//		writeVarInt(trace.getStepVariableTable().values().size());
//		for (StepVariableRelationEntry entry : trace.getStepVariableTable().values()) {
//			writeString(entry.getVarID());
//			writeVarInt(entry.getProducers().size());
//			for (TraceNode node : entry.getProducers()) {
//				writeVarInt(node.getOrder());
//				writeVarInt(WRITE);
//			}
//			writeVarInt(entry.getConsumers().size());
//			for (TraceNode node : entry.getConsumers()) {
//				writeVarInt(node.getOrder());
//				writeVarInt(READ);
//			}
//		}
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
