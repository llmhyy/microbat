package microbat.instrumentation.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import microbat.handler.xml.VarValueXmlWriter;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.utils.CollectionUtils;

public class TraceOutputWriter extends DataOutputStream {
	public static final int READ = 1;
	public static final int WRITE = 2;
	
	public TraceOutputWriter(OutputStream out) {
		super(out);
	}
	
	public void writeTrace(Trace trace) throws IOException {
		int traceNum = (trace == null ? 0 : 1);
		writeInt(traceNum);
		writeTrace(trace, null, null, null, null);
	}
	
	private void writeString(String str) throws IOException {
		if (str == null) {
			writeInt(-1);
		} else if ( str.isEmpty()) {
			writeInt(0);
		} else {
			writeInt(str.length());
			writeBytes(str);
		}
	}
	
	public void writeTrace(Trace trace, String projectName, String projectVersion, String launchClass,
			String launchMethod) throws IOException {
		writeString(projectName);
		writeString(projectVersion);
		writeString(launchClass);
		writeString(launchMethod);
		writeBoolean(trace.isMultiThread());
		writeSteps(trace.getExecutionList());
		writeStepVariableRelation(trace);
	}
	
	private void writeSteps(List<TraceNode> exectionList) throws IOException {
		writeInt(exectionList.size());
		for (int i = 0; i < exectionList.size(); i++) {
			TraceNode node = exectionList.get(i);
			writeLocation(node.getBreakPoint());
			writeNodeOrder(node.getControlDominator());
			writeNodeOrder(node.getStepInNext());
			writeNodeOrder(node.getStepOverNext());
			writeNodeOrder(node.getInvocationParent());
			writeNodeOrder(node.getLoopParent());
			writeString(generateXmlContent(node.getReadVariables()));
			writeString(generateXmlContent(node.getWrittenVariables()));
		}
	}

	private void writeNodeOrder(TraceNode node) throws IOException {
		if (node != null) {
			writeInt(node.getOrder());
		} else {
			writeInt(-1);
		}
	}

	protected String generateXmlContent(List<VarValue> varValues) {
		if (CollectionUtils.isEmpty(varValues)) {
			return null;
		}
		return VarValueXmlWriter.generateXmlContent(varValues);
	}
	
	private void writeStepVariableRelation(Trace trace) throws IOException {
		writeInt(trace.getStepVariableTable().values().size());
		for (StepVariableRelationEntry entry : trace.getStepVariableTable().values()) {
			writeString(entry.getVarID());
			writeInt(entry.getProducers().size());
			for (TraceNode node : entry.getProducers()) {
				writeInt(node.getOrder());
				writeInt(WRITE);
			}
			writeInt(entry.getConsumers().size());
			for (TraceNode node : entry.getConsumers()) {
				writeInt(node.getOrder());
				writeInt(READ);
			}
		}
	}
	
	private void writeLocation(BreakPoint location) throws IOException {
		writeString(location.getDeclaringCompilationUnitName());
		writeInt(location.getLineNumber());
		writeBoolean(location.isConditional());
		writeBoolean(location.isReturnStatement());
		writeConstrolScope(location.getControlScope());
		writeLoopScope(location.getLoopScope());
	}
	
	private void writeConstrolScope(ControlScope controlScope) throws IOException {
		if (controlScope != null && !controlScope.getRangeList().isEmpty()) {
			writeInt(controlScope.getRangeList().size());
			writeBoolean(controlScope.isLoop());
			for (ClassLocation controlLoc : controlScope.getRangeList()) {
				writeString(controlLoc.getClassCanonicalName());
				writeInt(controlLoc.getLineNumber());
			}
		} else {
			writeInt(0);
		}
	}
	
	private void writeLoopScope(SourceScope loopScope) throws IOException {
		if (loopScope == null) {
			writeInt(0);
		} else {
			writeInt(1);
			writeString(loopScope.getClassName());
			writeInt(loopScope.getStartLine());
			writeInt(loopScope.getEndLine());
		}
	}
}
