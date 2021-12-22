package microbat.sql;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import org.neo4j.driver.Driver;
import static org.neo4j.driver.Values.parameters;

import microbat.handler.xml.VarValueXmlWriter;
import sav.common.core.utils.CollectionUtils;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class GraphRecorder implements TraceRecorder {
	private final String runId;
	private final Driver driver;
	private static final String CONNECTION_URI = "bolt://localhost";
	private static final String CREATE_TRACE_QUERY = "CREATE (t: Trace) SET t = $props";
	private static final String CREATE_TRACE_STEP_RELATION = "MATCH (t: Trace), (s:Step) where t.traceId = s.traceId CREATE (t)-[:COMPRISES]->(s)";
	private static final String CREATE_STEP_IN_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.traceId = b.traceId AND a.stepIn= b.stepOrder CREATE (a)-[:STEP_IN]->(b)";
	private static final String CREATE_CONTROL_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.traceId = b.traceId AND a.stepOrder = b.controlDominator CREATE (a)-[:CONTROL_DOMINATES]->(b)";
	private static final String CREATE_INVOCATION_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.traceId = b.traceId AND a.stepOrder = b.invocationParent CREATE (a)-[:INVOKES]->(b)";
	private static final String CREATE_LOOP_RELATIONS = "MATCH (a: Step), (b: Step) WHERE a.traceId = b.traceId AND a.stepOrder = b.loopParent CREATE (a)-[:LOOPS]->(b)";
	private static final String CREATE_LOCATION_RELATIONS = "MATCH (a: Step), (b: Location) WHERE a.traceId = b.traceId AND a.locationId = b.locationId CREATE (a)-[:AT]->(b)";
	private static final String INSERT_STEPS_QUERY = "CREATE (n:Step) SET n = $props";
	private static final String INSERT_LOCATION = "MERGE (a:Location {locationId: $locationId, traceId: $traceId, className: $className, lineNumber: $lineNumber, isConditional: $isConditional, isReturn: $isReturn})";

	public GraphRecorder(String runId) {
		this.runId = runId;
		driver = GraphDatabase.driver(CONNECTION_URI, AuthTokens.basic("neo4j", "microbat"));
	}

	private Result insertLocation(final Transaction tx, final BreakPoint bp, final String traceId) {
		Map<String, Object> props = new HashMap<>();
		props.put("locationId", bp.getDeclaringCompilationUnitName() + "_" + bp.getLineNumber());
		props.put("traceId", traceId);
		props.put("className", bp.getDeclaringCompilationUnitName());
		props.put("lineNumber", bp.getLineNumber());
		props.put("isConditional", bp.isConditional());
		props.put("isReturn", bp.isReturnStatement());
		// Location might be batch inserted since the MERGE check takes a longer time to
		// complete than CREATE
		return tx.run(INSERT_LOCATION, props);
	}

	private void insertSteps(final Session session, final Trace trace, final String traceId) {
		for (TraceNode node : trace.getExecutionList()) {
			Map<String, Object> props = new HashMap<>();
			props.put("traceId", traceId);
			props.put("stepOrder", node.getOrder());
			Optional.ofNullable(node.getControlDominator())
					.ifPresent(cd -> props.put("controlDominator", cd.getOrder()));
			Optional.ofNullable(node.getStepInNext()).ifPresent(cd -> props.put("stepIn", cd.getOrder()));
			Optional.ofNullable(node.getStepOverNext()).ifPresent(cd -> props.put("stepOver", cd.getOrder()));
			Optional.ofNullable(node.getInvocationParent())
					.ifPresent(cd -> props.put("invocationParent", cd.getOrder()));
			Optional.ofNullable(node.getLoopParent()).ifPresent(cd -> props.put("loopParent", cd.getOrder()));
			props.put("locationId", node.getDeclaringCompilationUnitName() + "_" + node.getLineNumber());
			props.put("time", LocalDateTime.ofEpochSecond(node.getTimestamp(), 0, ZoneOffset.UTC));
			Optional.ofNullable(generateXmlContent(node.getReadVariables()))
					.ifPresent(xmlContent -> props.put("readVariables", xmlContent));
			Optional.ofNullable(generateXmlContent(node.getWrittenVariables()))
					.ifPresent(xmlContent -> props.put("writeVariables", xmlContent));
			session.writeTransaction(tx -> tx.run(INSERT_STEPS_QUERY, parameters("props", props)));
			BreakPoint bp = node.getBreakPoint();
			session.writeTransaction(tx -> insertLocation(tx, bp, traceId));
		}
	}

	@Override
	public void store(List<Trace> traces) {
		try (Session session = driver.session()) {
			for (Trace trace : traces) {
				String traceId = UUID.randomUUID().toString();
				Map<String, Object> props = new HashMap<>();
				props.put("runId", runId);
				props.put("traceId", traceId);
				props.put("threadId", trace.getThreadId());
				props.put("threadName", trace.getThreadName());
				props.put("isMain", trace.isMain());
				session.writeTransaction(tx -> tx.run(CREATE_TRACE_QUERY, parameters("props", props)));
				insertSteps(session, trace, traceId);
			}
			session.writeTransaction(tx -> tx.run(CREATE_STEP_IN_RELATIONS));
			session.writeTransaction(tx -> tx.run(CREATE_CONTROL_RELATIONS));
			session.writeTransaction(tx -> tx.run(CREATE_INVOCATION_RELATIONS));
			session.writeTransaction(tx -> tx.run(CREATE_LOOP_RELATIONS));
			session.writeTransaction(tx -> tx.run(CREATE_LOCATION_RELATIONS));
			session.writeTransaction(tx -> tx.run(CREATE_TRACE_STEP_RELATION));
		}
	}

	protected String generateXmlContent(Collection<VarValue> varValues) {
		if (CollectionUtils.isEmpty(varValues)) {
			return null;
		}
		return VarValueXmlWriter.generateXmlContent(varValues);
	}

	@Override
	public void insertSteps(String traceId, List<TraceNode> traces) {
		try (Session session = driver.session()) {
			Map<String, Object> props = new HashMap<>();
			props.put("traceId", traceId);
			session.writeTransaction(tx -> tx.run(CREATE_TRACE_QUERY, parameters("props", props)));
		}
	}
}
