package microbat.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.neo4j.driver.Driver;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import static org.neo4j.driver.Values.parameters;

import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.Pair;

public class GraphTraceRetriever implements TraceRetriever {
	private final String runId;
	private Driver driver;
	private static final String CONNECTION_URI = "bolt://localhost";
	private static final String GET_TRACES_QUERY = "MATCH (t:Trace) WHERE t.runId = $runId RETURN t";
	private static final String GET_STEPS_QUERY = "MATCH (s:Step)-[:AT]->(l:Location) WHERE s.traceId = $traceId RETURN s,l ORDER BY s.stepOrder";
	private static final String GET_LOCATIONS_QUERY = "MATCH (l:Location) WHERE l.traceId = $traceId RETURN l";
	private Map<Integer, Integer> controlDominator = new HashMap<>();
	private Map<Integer, Integer> stepIn = new HashMap<>();
	private Map<Integer, Integer> stepOver = new HashMap<>();
	private Map<Integer, Integer> invocationParent = new HashMap<>();
	private Map<Integer, Integer> loopParent = new HashMap<>();
	private Map<Integer, TraceNode> map = new HashMap<>();
	private Map<String, BreakPoint> locationsMap = new HashMap<>();

	public GraphTraceRetriever(String runId) {
		this.runId = runId;
		this.driver = GraphDatabase.driver(CONNECTION_URI, AuthTokens.basic("neo4j", "microbat"));
	}
	
	private TraceNode buildStep(final Record rec, final Trace trace) {
		int currOrder = rec.values().get(0).get("stepOrder").asInt();
		TraceNode step = new TraceNode(null, null, currOrder, trace);
		Optional.of(rec.values().get(0).get("controlDominator")).filter(x -> !x.isNull()).ifPresent(target -> controlDominator.put(currOrder, target.asInt()));;
		Optional.of(rec.values().get(0).get("stepIn")).filter(x -> !x.isNull()).ifPresent(target -> stepIn.put(currOrder, target.asInt()));;
		Optional.of(rec.values().get(0).get("stepOver")).filter(x -> !x.isNull()).ifPresent(target -> stepOver.put(currOrder, target.asInt()));;
		Optional.of(rec.values().get(0).get("invocationParent")).filter(x -> !x.isNull()).ifPresent(target -> invocationParent.put(currOrder, target.asInt()));;
		Optional.of(rec.values().get(0).get("loopParent")).filter(x -> !x.isNull()).ifPresent(target -> loopParent.put(currOrder, target.asInt()));;
		step.setTimestamp(rec.values().get(0).get("time").asLocalDateTime().toEpochSecond(ZoneOffset.UTC));
		map.put(currOrder, step);
		String locationId = rec.values().get(1).get("locationId").asString();
		if (!locationsMap.containsKey(locationId)) {
			String className = rec.values().get(1).get("className").asString();
			int lineNumber = rec.values().get(1).get("lineNumber").asInt();
			BreakPoint bp = new BreakPoint(className, className, lineNumber);
			bp.setConditional(rec.values().get(1).get("isConditional").asBoolean());
			bp.setReturnStatement(rec.values().get(1).get("isReturn").asBoolean());
			locationsMap.put(locationId, bp);
		}
		step.setBreakPoint(locationsMap.get(locationId));
		return step;
	}
	
	private List<TraceNode> getSteps(final Transaction tx, final String traceId, final Trace trace) {
		Result res = tx.run(GET_STEPS_QUERY, parameters("traceId", traceId));
		List<TraceNode> nodes = res.list(rec -> buildStep(rec, trace));
		buildNodeRelations(nodes);
		return nodes;
	}
	
	private void buildNodeRelations(List<TraceNode> nodes) {
		for (Map.Entry<Integer, Integer> entry: controlDominator.entrySet()) {
			TraceNode a = map.get(entry.getKey());
			TraceNode b = map.get(entry.getValue());
			a.setControlDominator(b);
			b.addControlDominatee(a);
//			map.get(entry.getKey()).setControlDominator(map.get(entry.getValue()));
//			map.get(entry.getValue()).addControlDominatee(map.get(entry.getKey()));
		}
		for (Map.Entry<Integer, Integer> entry: stepIn.entrySet()) {
			map.get(entry.getKey()).setStepInNext(map.get(entry.getValue()));
			map.get(entry.getValue()).setStepInPrevious(map.get(entry.getKey()));
		}
		for (Map.Entry<Integer, Integer> entry: stepOver.entrySet()) {
			map.get(entry.getKey()).setStepOverNext(map.get(entry.getValue()));
			map.get(entry.getValue()).setStepOverPrevious(map.get(entry.getKey()));
		}
		for (Map.Entry<Integer, Integer> entry: invocationParent.entrySet()) {
			map.get(entry.getKey()).setInvocationParent(map.get(entry.getValue()));
			map.get(entry.getValue()).addInvocationChild(map.get(entry.getKey()));
		}
		for (Map.Entry<Integer, Integer> entry: loopParent.entrySet()) {
			map.get(entry.getKey()).setLoopParent(map.get(entry.getValue()));
			map.get(entry.getValue()).addLoopChild(map.get(entry.getKey()));
		}
	}

	@Override
	public List<Trace> getTraces(String runId) {
		try (Session session = driver.session()) {
			List<Trace> traces = new ArrayList<>();
			Result res = session.run(GET_TRACES_QUERY, parameters("runId", runId));
			for (Record rec : res.list()) {
				String traceId = rec.values().get(0).get("traceId").asString();
				Trace trace = new Trace(null, traceId);
				trace.setThreadId(rec.values().get(0).get("threadId").asLong());
				trace.setThreadName(rec.values().get(0).get("threadName").asString());
				trace.setMain(rec.values().get(0).get("isMain").asBoolean());
				List<TraceNode> nodes = session.readTransaction(tx -> getSteps(tx, trace.getId(), trace));
				trace.setExecutionList(nodes);
				traces.add(trace);
			}
			return traces;
		}
	}

	@Override
	public Pair<List<VarValue>, List<VarValue>> loadRWVars(TraceNode step, String traceId) {
//			List<VarValue>readVars = toVarValue(rs.getString("read_vars"));
//			// written_vars
//			loadVarStep = "written_vars";
//			List<VarValue>writeVars = toVarValue(rs.getString("written_vars"));
//			return Pair.of(readVars, writeVars);
		return null;
	}
}
