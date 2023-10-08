package microbat.vectorization;

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
import microbat.vectorization.vector.NodeVector;
import microbat.vectorization.vector.Vector;


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
}
