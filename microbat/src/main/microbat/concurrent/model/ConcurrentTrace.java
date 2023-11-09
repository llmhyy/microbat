package microbat.concurrent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import sav.strategies.dto.AppJavaClassPath;

public class ConcurrentTrace extends Trace {
	
	private List<Trace> originalTraces;
	// temporary storage for processing
	private List<ConcurrentTraceNode> generatedTraceNodes;
	// the non sequential concurrent trace nodes
	private ArrayList<ArrayList<ConcurrentTraceNode>> traces
		= new ArrayList<>();
	
	public List<ConcurrentTraceNode> getSequentialTrace() {
		return generatedTraceNodes;
	}
	
	public ConcurrentTrace(AppJavaClassPath appJavaClassPath) {
		super(appJavaClassPath);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Technique to update trace nodes values
	 */
	private void postProcess() {
		for (int i = 0; i < generatedTraceNodes.size(); ++i) {
			generatedTraceNodes.get(i).setOrder(i);
			this.addTraceNode(generatedTraceNodes.get(i));
		}
	}
	
	/**
	 * Function used to generate concurrent read write nodes.
	 * Need to split the trace nodes into read and write.
	 */
	private void generateTraces() {
		if (traces.size() != 0) throw new RuntimeException("Should not generate more than once");
		if (originalTraces == null) throw new RuntimeException("Original traces not set");
		traces.ensureCapacity(originalTraces.size());
		int ctr = 0;
		for (Trace trace: originalTraces) {
			ArrayList<ConcurrentTraceNode> result = new ArrayList<>();
			// reserve double the size than expected
			result.ensureCapacity(trace.getExecutionList().size() * 2);
			for (int i = 0; i < trace.getExecutionList().size(); ++i) {
				TraceNode traceNode = trace.getExecutionList().get(i);
				result.addAll(ConcurrentTraceNode.splitTraceNode(traceNode, ctr));
				
			}
			ctr += 1;
			traces.add(result);
		}
		// update the step in previous and step in next
		for (ArrayList<ConcurrentTraceNode> trace: traces) {
			ConcurrentTraceNode previous = null;
			for (int i = 0; i < trace.size(); ++i) {
				if (previous != null) {
					trace.get(i).setStepInPrevious(trace.get(i-1));
				}
				if (i != trace.size() - 1) {
					trace.get(i).setStepInNext(trace.get(i+1));
				}
				previous = trace.get(i);
			}
		}
	}
	
	/**
	 * Generates a concurrent trace from the given trace by timestamp order.
	 * End goal is to remove timestamp synchronisation - aka deprecate this.
	 * @param traces The trace to generate the trace from
	 * @return
	 */
	public static ConcurrentTrace fromTimeStampOrder(List<Trace> inputTraces) {
		ConcurrentTrace resultTrace = new ConcurrentTrace("");
		resultTrace.originalTraces = inputTraces;
		resultTrace.generateTraces();
		ArrayList<ArrayList<ConcurrentTraceNode>> traces = resultTrace.traces;

		ArrayList<ConcurrentTraceNode> result = new ArrayList<>();
		resultTrace.generatedTraceNodes = result;
		List<VarValue> sharedVarIds = getSharedVarIDs(inputTraces);
		HashSet<String> sharedVarAliasIds = new HashSet<>();
		for (VarValue val: sharedVarIds) {
			sharedVarAliasIds.add(val.getAliasVarID());
		}
		HashMap<String, String> sharedVarValues = new HashMap<>();
		ArrayList<Integer> tracesPtr = new ArrayList<>();
		tracesPtr.ensureCapacity(traces.size());
		for (int i = 0; i < traces.size(); ++i) {
			tracesPtr.add(0); // point to the first trace node
		}
		while (true) {
			// select the first trace based off of time stamp order
			// if there is a tie, break tie based off of current shared variables status
			ArrayList<ConcurrentTraceNode> possibleNodes = new ArrayList<>();
			for (int i = 0; i < traces.size(); ++i) {
				ArrayList<ConcurrentTraceNode> trace = traces.get(i);
				int currentPoint = tracesPtr.get(i);
				if (currentPoint >= trace.size()) {
					// deal with case when all the pointers are at the end of the trace
					continue;
				}
				ConcurrentTraceNode m = trace.get(currentPoint);
				if (possibleNodes.size() == 0) {
					possibleNodes.add(m);
				} else {
					if (possibleNodes.get(0).getTimestamp() < m.getTimestamp()) {
						continue;
					}
					if (possibleNodes.get(0).getTimestamp() == m.getTimestamp()) {
						possibleNodes.add(m);
						continue;
					}
					// when all possible nodes are larger
					possibleNodes.clear();
					possibleNodes.add(m);
				}
			}
			if (possibleNodes.size() == 0) {
				break;
			}

			// greedy resolution -> pick the first
			result.add(possibleNodes.get(0));
			int id = possibleNodes.get(0).getCurrentTraceId();
			tracesPtr.set(id, tracesPtr.get(id) + 1);
			// todo: proper resolution
			// deal with possible nodes.
			// allocate the first possible node
			// will be the bulk of the logic without timestamps
			//ConcurrentTraceNode selected = extractPossibleTraceNode(result, sharedVarAliasIds, sharedVarValues, possibleNodes);
			//tracesPtr.set(selected.getCurrentTraceId(), tracesPtr.get(selected.getCurrentTraceId()) + 1);
		}
		
		resultTrace.postProcess();
		
		return resultTrace;
	}

	/**
	 * Uses a SMT to solve W -> R constraints
	 * Constraints generated by currently available information on read and write.
	 */
	private void SMTProcessing() {
		
	}
	
	/**
	 * Extracts a tracenode
	 * @param result
	 * @param sharedVarAliasIds
	 * @param sharedVarValues
	 * @param possibleNodes
	 * @return
	 */
	private static ConcurrentTraceNode extractPossibleTraceNode(ArrayList<ConcurrentTraceNode> result,
			HashSet<String> sharedVarAliasIds, HashMap<String, String> sharedVarValues,
			ArrayList<ConcurrentTraceNode> possibleNodes) {
		ConcurrentTraceNode selected = null;
		for (int i = 0; i < possibleNodes.size(); ++i) {
			ConcurrentTraceNode current = possibleNodes.get(i);
			if (current.getWrittenVariables().size() > 0) {
				continue;
			}
			List<VarValue> readValues = current.getReadVariables();
			// check the string value to if satis
			boolean satis = true;
			for (VarValue val: readValues) {
				assert(val.getAliasVarID() == null || (!sharedVarAliasIds.contains(val.getAliasVarID()) 
						|| sharedVarValues.containsKey(val.getAliasVarID())));
				// when there isn't a match
				if (val.getAliasVarID() != null && sharedVarAliasIds.contains(val.getAliasVarID())
						&& !val.getStringValue().equals(sharedVarValues.get(val.getAliasVarID()))) {
					satis = false;
					break;
				}
			}
			if (satis) {
				selected = current;
			}
		}
		if (selected != null ) {
			result.add(selected);
			return selected;
		}
		// todo: this resolution is too greedy
		// it doesn't work as we do not preserve W -> R
		// may need to use some kind of constraint solving
		// may not be possible until we indicate in the instrumentation
		// to record RC vector (To determine what reads must occur before this write)
		for (int i = 0; i < possibleNodes.size(); ++i) {
			ConcurrentTraceNode current = possibleNodes.get(i);
			if (current.getWrittenVariables().size() > 0) {
				// take the first write
				selected = current;
				break;
			}
		}
		if (selected == null) {
			throw new RuntimeException("Failed to allocate trace node to concurrent trace");
		}
		return selected;
	}
	

	
	 

	@Override
	public TraceNode getTraceNode(int order) {
		// TODO Auto-generated method stub
		return this.getSequentialTrace().get(order);
	}
	
	public void constructControlDominator() {
		
	}

	/**
	 * Finds data dependency relative to the concurrent trace
	 */
	@Override
	public TraceNode findDataDependency(TraceNode checkingNode, VarValue readVar) {
		int j = checkingNode.getOrder();
		// check if it is on heap
		String s = readVar.getAliasVarID();
		boolean isOnHeap = s != null;
		if (!(checkingNode instanceof ConcurrentTraceNode)) {
			throw new RuntimeException("Wrong checking node type");
		}
		ConcurrentTraceNode concNode = (ConcurrentTraceNode) checkingNode;

		// find the individual trace to use the find data dependenccy
		String varID = Variable.truncateSimpleID(readVar.getVarID());
		String headID = Variable.truncateSimpleID(readVar.getAliasVarID());
			
		for(int i=checkingNode.getOrder()-1; i>=1; i--) {
			ConcurrentTraceNode node = this.getSequentialTrace().get(i);
			for(VarValue writtenValue: node.getWrittenVariables()) {
				
				String wVarID = Variable.truncateSimpleID(writtenValue.getVarID());
				String wHeadID = Variable.truncateSimpleID(writtenValue.getAliasVarID());
				
				if(wVarID != null && wVarID.equals(varID) && node.getCurrentTraceId() 
						== concNode.getCurrentTraceId()) {
					return node;						
				}
				
				if(wHeadID != null && wHeadID.equals(headID)) {
					return node;
				}
				
				VarValue childValue = writtenValue.findVarValue(varID, headID);
				if(childValue != null) {
					return node;
				}
				
			}
		}
		return null;
	}



	/**
	 * 
	 */
	@Override
	public List<TraceNode> findDataDependentee(TraceNode traceNode, VarValue writtenVar) {
		List<TraceNode> consumers = new ArrayList<TraceNode>();
		
		String varID = Variable.truncateSimpleID(writtenVar.getVarID());
		String headID = Variable.truncateSimpleID(writtenVar.getAliasVarID());
		
		for(int i=traceNode.getOrder()+1; i <= this.getSequentialTrace().size(); i++) {
			TraceNode node = this.getSequentialTrace().get(i);
			for(VarValue readVar: node.getReadVariables()) {
				
				String rVarID = Variable.truncateSimpleID(readVar.getVarID());
				String rHeadID = Variable.truncateSimpleID(readVar.getAliasVarID());
//				
//				if (readVar.equals(writtenVar)) {
//					consumers.add(node);
//				}
				if(rVarID != null && rVarID.equals(varID)) {
					consumers.add(node);						
				}
				
				if(rHeadID != null && rHeadID.equals(headID)) {
					consumers.add(node);
				}
				
				VarValue childValue = readVar.findVarValue(varID, headID);
				if(childValue != null) {
					consumers.add(node);
				}
				
			}
		}
		return consumers;
		
	}



	public ConcurrentTrace(String id) {
		super(id);
	}
	
	public static ConcurrentTrace fromTraces(List<Trace> traces) {
		return null;
	}
	
	private List<TraceNode> generateSequential(List<Trace> traces) {
		return null;
	}
	
	// convert the trace nodes to have read only and write only
	private static List<TraceNode> convertTraceNodes(Trace trace) {
		ArrayList<TraceNode> result = new ArrayList<>();
		for (int i = 0; i < trace.size(); ++i) {
			TraceNode current = trace.getTraceNode(i);
		}
		return null;
	}

	
//	private List<ConcurrentTraceNode> fromSequential(List<TraceNode> generatedNodes) {
//		
//	}
	
	private static List<VarValue> getSharedVarIDs(List<Trace> traces) {
		HashSet<String> sharedVariables = new HashSet<>();
		List<VarValue> result = new ArrayList<>();
		for (Trace trace: traces) {
			for (TraceNode tnode: trace.getExecutionList()) {
				List<VarValue> writtenValues = tnode.getWrittenVariables();
				List<VarValue> readValues = tnode.getReadVariables();
				List<VarValue> combined = new ArrayList<>();
				for (VarValue wVal: writtenValues) {
					combined.add(wVal);
				}
				for (VarValue rVal: readValues) {
					combined.add(rVal);
				}
				for (VarValue value: combined) {
					if (value.getAliasVarID() == null) continue;
					if (sharedVariables.contains(value.getAliasVarID())) {
						result.add(value);
						continue;
					}
					sharedVariables.add(value.getAliasVarID());
				}
			}
		}
		return result;
	}
	
	/**
	 * Creates a virtual trace given a concurrent trace
	 * @return
	 */
	private static List<TraceNode> constructSequentialTrace(List<Trace> traces) {
		Set<VarValue> sharedVariables = new HashSet<>();
		HashMap<String, String> valueMap = new HashMap<>();
		
		sharedVariables.addAll(getSharedVarIDs(traces));
		for (VarValue sharedVar: sharedVariables) {
			valueMap.put(sharedVar.getAliasVarID(), "");
		}
		
		ArrayList<TraceNode> sequentialTrace = new ArrayList<>();
		// split all the traces into read + write trace nodes
		
		int[] startingPtr = new int[traces.size()];
		for (int i = 0; i < traces.size(); ++i) {
			startingPtr[i] = 0;
		}

		while (true) {
			// keep track of all traces -> if none of them have a matching node -> we can make no progress
			boolean allNotStatis = true;
			for (int i = 0; i < traces.size(); ++i) {
				TraceNode current = traces.get(i).getTraceNode(startingPtr[i]);
				List<VarValue> readValues = current.getReadVariables();
				List<VarValue> writeValues = current.getWrittenVariables();
				boolean satis = true;
				// check if the read value matches the current variables value
				for (VarValue val: readValues) {
					if (sharedVariables.contains(val)
							&& valueMap.get(val) != val.getStringValue()) {
						satis = false;
						break;
					}
				}
				if (!satis) continue;
				for (int k = 0; k < traces.size(); ++k) {
					if (k == i) continue;
					
				}
				// todo: check if this is blocking the other points
				startingPtr[i] += 1;
				sequentialTrace.add(current);
				break;
			}
			boolean completed = true;
			for (int i = 0; i < traces.size(); ++i) {
				if (startingPtr[i] < traces.get(i).size()) {
					completed = false;
					break;
				}
			}
			if (completed) break;
		}
		return sequentialTrace;
		
	}
}
