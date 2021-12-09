package microbat.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import microbat.handler.xml.VarValueXmlWriter;
import microbat.instrumentation.AgentParams;
import microbat.model.BreakPoint;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.utils.CollectionUtils;

/**
 * @author dingyuchen
 *
 */
public class IntervalRecorder {

	private final TraceRecorder recorderInstance;
	private final int threshold;

	/**
	 * @param dbPath
	 */
	public IntervalRecorder(int threshold, TraceRecorder recorder) {
		this.threshold = threshold;
		this.recorderInstance = recorder;
	}

	public void partialStore(String traceId, List<TraceNode> traces) {
		recorderInstance.insertSteps(traceId, traces);
	}
	
	public int getThreshold() {
		return threshold;
	}
}
