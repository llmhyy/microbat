package microbat.probability.SPP.vectorization;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.FileWriter;
import java.io.IOException;
import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.SPP.vectorization.vector.NodeVector;
import microbat.probability.SPP.vectorization.vector.Vector;


public class TraceVectorizer {

	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	
	public TraceVectorizer() {
		this.constructUnmodifiedOpcodeType();
	}
	
	private void constructUnmodifiedOpcodeType() {
		this.unmodifiedType.add(OpcodeType.LOAD_CONSTANT);
		this.unmodifiedType.add(OpcodeType.LOAD_FROM_ARRAY);
		this.unmodifiedType.add(OpcodeType.LOAD_VARIABLE);
		this.unmodifiedType.add(OpcodeType.STORE_INTO_ARRAY);
		this.unmodifiedType.add(OpcodeType.STORE_VARIABLE);
		this.unmodifiedType.add(OpcodeType.RETURN);
		this.unmodifiedType.add(OpcodeType.GET_FIELD);
		this.unmodifiedType.add(OpcodeType.GET_STATIC_FIELD);
		this.unmodifiedType.add(OpcodeType.PUT_FIELD);
		this.unmodifiedType.add(OpcodeType.PUT_STATIC_FIELD);
		this.unmodifiedType.add(OpcodeType.INVOKE);
	}
	
	public List<NodeFeatureRecord> vectorize(final Trace trace) {
//		this.preprocess(trace);
		List<NodeFeatureRecord> records = new ArrayList<>();
		Set<Vector> existingVectors = new HashSet<>();
		for (TraceNode node : trace.getExecutionList()) {
			if (node.getBytecode() == "") {
				continue;
			}

			Vector vector = null;
			try {
				vector = new NodeVector(node);
//				vector = new ContextVector(node, trace);
			} catch (Exception e) {
				vector = new NodeVector();
				System.out.println("[TraceVectorization] error occur when forming vector");
			}
			
			if (this.containInvalidValue(vector.getVector()) ) {
				continue;
			}
			
			if (existingVectors.contains(vector)) {
				continue;
			}
			
			existingVectors.add(vector);
//			ClassificationVector classificationVector = new ClassificationVector(node);
			NodeFeatureRecord record = new NodeFeatureRecord(node, vector);
			records.add(record);
		}
		return records;
		
	}
	
	private boolean containInvalidValue(final float[] vector) {
		for (float element : vector) {
			if (Float.isNaN(element) || Float.isInfinite(element)) {
				return true;
			}
		}
		return false;
	}
	
	public void wirteToFile(List<NodeFeatureRecord> records, final String path) {
		try {
			System.out.println("Writing feature to " + path);
			FileWriter writer = new FileWriter(path);
			for (NodeFeatureRecord record : records) {
				writer.write(record.toString());
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void preprocess(final Trace trace) {
		this.countRepeat(trace);
		this.computeCost(trace);
	}
	
	private void countRepeat(final Trace trace) {
		Map<BreakPoint, Integer> repeatedCounts = new HashMap<>();
		for (TraceNode node : trace.getExecutionList()) {
			final BreakPoint bkp = node.getBreakPoint();
			repeatedCounts.put(bkp, 
				repeatedCounts.containsKey(bkp) ?
				repeatedCounts.get(bkp) + 1:
				0
			);
			node.repeatedCount = repeatedCounts.get(bkp);
		}
	}
	
	private void computeCost(final Trace trace) {
		// First count the computational operations for each step and normalize
		// Use addExact here to handle overflow issue
		final long totalNodeCost = trace.getExecutionList().stream()
				.mapToLong(node -> this.countModifyOperation(node))
				.sum();
		
		trace.getExecutionList().stream()
		 	.forEach(node -> node.computationCost = this.countModifyOperation(node) / ((double) totalNodeCost));
		
		// Init computational cost of all variable to 0.0
		trace.getExecutionList().stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost = 0.0d);
		trace.getExecutionList().stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost = 0.0d);

		double maxVarCost = 0.0f;
		for (TraceNode node : trace.getExecutionList()) {
			
			// Skip if there are no read variable (do not count "this" variable)
			List<VarValue> readVars = node.getReadVariables().stream().filter(var -> !var.isThisVariable()).collect(Collectors.toList());
			if (readVars.size() == 0) {
				continue;
			}
			
			// Inherit computational cost
			for (VarValue readVar : node.getReadVariables()) {
				final VarValue dataDomVar = this.findDataDomVar(trace, readVar, node);
				if (dataDomVar != null) {
					readVar.computationalCost = dataDomVar.computationalCost;
				}
			}
			
			// Sum up the cost of all read variable, excluding "this" variable
			final double cumulatedCost = node.getReadVariables().stream().filter(var -> !var.isThisVariable())
					.mapToDouble(var -> var.computationalCost)
					.sum();
			final double optCost = node.computationCost;
			final double cost = Double.isInfinite(cumulatedCost+optCost) ? Double.MAX_VALUE : cumulatedCost+optCost;
			
			// Assign computational cost to written variable, excluding "this" variable
			node.getWrittenVariables().stream().filter(var -> !var.isThisVariable()).forEach(var -> var.computationalCost = cost);
			maxVarCost = Math.max(cost, maxVarCost);
		}
		
		final double maxVarCost_ = maxVarCost;
		trace.getExecutionList().stream().flatMap(node -> node.getReadVariables().stream()).forEach(var -> var.computationalCost /= maxVarCost_);
		trace.getExecutionList().stream().flatMap(node -> node.getWrittenVariables().stream()).forEach(var -> var.computationalCost /= maxVarCost_);
	}
	
	private VarValue findDataDomVar(final Trace trace, final VarValue var, final TraceNode node) {
		TraceNode dataDominator = trace.findDataDependency(node, var);
		if (dataDominator != null) {
			for (VarValue writeVar : dataDominator.getWrittenVariables()) {
				if (writeVar.equals(var)) {
					return writeVar;
				}
			}
		}
		return null;
	}
	
	private int countModifyOperation(final TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		int count = 0;
		for (ByteCode byteCode : byteCodeList) {
			if (!this.unmodifiedType.contains(byteCode.getOpcodeType())) {
				count+=1;
			}
		}
		return count;
	}
}
