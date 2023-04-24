package microbat.probability.SPP.vectorization.vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.trace.TraceNode;

public class FunctionVector extends Vector {
	
	public static final int INVOKE_TYPE_COUNT = 5;
	public static final int INPUT_PARAM_COUNT = 10;

	
	// Add 1 for library calling function
	public static final int DIMENSION = FunctionVector.INVOKE_TYPE_COUNT + 
										InputParameterVector.DIMENSION * FunctionVector.INPUT_PARAM_COUNT + 
										OutputParameterVector.DIMENSION + 
										1;

	private static final String LIBRARY_CLASSES_PATH = "./src/main/microbat/probability/SPP/vectorization/vector/java_11_classes.txt";
	private static final Set<String> LIBRARY_CLASSES = FunctionVector.readLibraryClasses();
	
	private final InputParameterVector[] inputParamTypeVectors;
	private final OutputParameterVector outputParamTypeVector;
	
	private static final int DYNAMIC_IDX = 0;
	private static final int INTERFACE_IDX = 1;
	private static final int SPECIAL_IDX = 2;
	private static final int STATIC_IDX = 3;
	private static final int VIRTUAL_IDX = 4;
	private static final int LIBRARY_FUNC_IDX = 5;
	
	public FunctionVector() {
		super(new float[FunctionVector.DIMENSION]);
		Arrays.fill(this.vector, 0.0f);
		this.inputParamTypeVectors = new InputParameterVector[FunctionVector.INPUT_PARAM_COUNT];
		for (int idx=0; idx<this.inputParamTypeVectors.length; idx++) {
			this.inputParamTypeVectors[idx] = new InputParameterVector();
		}
		this.outputParamTypeVector = new OutputParameterVector();
	}
	
	public FunctionVector(final ByteCode byteCode, final String funcSign) {
		super(new float[FunctionVector.INVOKE_TYPE_COUNT+1]);
		Arrays.fill(this.vector, 0.0f);
		
		final short optCode = byteCode.getOpcode();
		if (this.isInvokeDynamic(optCode)) this.set(FunctionVector.DYNAMIC_IDX);
		if (this.isInvokeInterface(optCode)) this.set(FunctionVector.INTERFACE_IDX);
		if (this.isInvokeSpecial(optCode)) this.set(FunctionVector.SPECIAL_IDX);
		if (this.isInvokeStatic(optCode)) this.set(FunctionVector.STATIC_IDX);
		if (this.isInvokeVirtual(optCode)) this.set(FunctionVector.VIRTUAL_IDX);
		
		final String classname = this.extractClassName(funcSign);
		if (FunctionVector.isLibClass(classname)) this.set(LIBRARY_FUNC_IDX);
		System.out.println("classname: " + classname);
		
		final String inputTypeDescriptors = this.extractInputTypeDescriptors(funcSign);
		this.inputParamTypeVectors = InputParameterVector.constructVectors(inputTypeDescriptors, FunctionVector.INPUT_PARAM_COUNT);
		for (InputParameterVector vector : this.inputParamTypeVectors) {
			this.vector = ArrayUtils.addAll(this.vector, vector.getVector());
		}
		System.out.println("input: " + inputTypeDescriptors);
		
		final String returnTypeDescriptors = this.extractReturnTypeDescriptors(funcSign);
		this.outputParamTypeVector = new OutputParameterVector(returnTypeDescriptors);
		this.vector = ArrayUtils.addAll(this.vector, this.outputParamTypeVector.getVector());
		System.out.println("output: " + returnTypeDescriptors);
	}
	
	private String extractClassName(final String funcSign) {
		return funcSign.split("#")[0];
	}
	
	private String extractInputTypeDescriptors(final String funcSign) {
		return funcSign.substring(funcSign.indexOf("(")+1, funcSign.indexOf(")"));
	}
	
	private String extractReturnTypeDescriptors(final String funcSign) {
		return funcSign.substring(funcSign.indexOf(")")+1);
	}
	
	private boolean isInvokeDynamic(final short optCode) {
		return optCode == 0xba;
	}
	
	private boolean isInvokeInterface(final short optCode) {
		return optCode == 0xb9;
	}
	
	private boolean isInvokeSpecial(final short optCode) {
		return optCode == 0xb7;
	}
	
	private boolean isInvokeStatic(final short optCode) {
		return optCode == 0xb8;
	}
	
	private boolean isInvokeVirtual(final short optCode) {
		return optCode == 0xb6;
	}
	
	public static FunctionVector[] constructFuncVectors(final TraceNode node, final int vectorCount) {
		if (node.getOrder() == 5 || node.getOrder() == 10) {
			System.out.println();
		}
		FunctionVector[] funcVectors = new FunctionVector[vectorCount];
	
		ByteCodeList byteCodeList_1 = new ByteCodeList(node.getBytecode());
		List<String> funcSigns = new ArrayList<>();
		List<ByteCode> invokeCodes = new ArrayList<>();
		
	
		for (ByteCode byteCode : byteCodeList_1) {
			if (byteCode.getOpcodeType().equals(OpcodeType.INVOKE)) {
				invokeCodes.add(byteCode);
			}
		}
		
		for (String funcSign : node.getInvokingMethod().split("%")) {
			if (funcSign != "") {
				funcSigns.add(funcSign);
			}
		}
		
		// Need to check the matched node as sometimes it may contains part of the byte code or function signature
		final TraceNode matchedNode = node.getInvokingMatchNode();
		if (matchedNode != null) {
			if (matchedNode.getOrder() != node.getOrder()) {
				ByteCodeList byteCodeList_2 = new ByteCodeList(matchedNode.getBytecode());
				
				for (ByteCode byteCode : byteCodeList_2) {
					if (byteCode.getOpcodeType().equals(OpcodeType.INVOKE)) {
						invokeCodes.add(byteCode);
					}
				}

				for (String funcSign : matchedNode.getInvokingMethod().split(";")) {
					if (funcSign != "") {
						funcSigns.add(funcSign);
					}
				}
			}
		} else {
			System.out.println("Don't have matched node");
		}


		
		if (funcSigns.size() != invokeCodes.size()) {
			throw new FunctionMismatchException("Invokation not match: node " + node.getOrder());
		}
		
		for (int idx=0; idx<funcVectors.length; idx++) {
			if (idx < funcSigns.size()) {
				final ByteCode byteCode = invokeCodes.get(idx);
				final String funcSign = funcSigns.get(idx);
				funcVectors[idx] = new FunctionVector(byteCode, funcSign);
			} else {
				funcVectors[idx] = new FunctionVector();
			} 
		}
		return funcVectors;
	}
	
	public List<String> splitMethodSigns(final String funcSigns) {
		List<String> funcSign_list = new ArrayList<>();
		String tempStr = "";
		for (char c : funcSigns.toCharArray()) {
			
		}
		return funcSign_list;
	}
	
	public static boolean isLibClass(final String type) {
		return FunctionVector.LIBRARY_CLASSES.contains(type);
	}
	
	private static Set<String> readLibraryClasses() {
		Set<String> classes = new HashSet<>();
		BufferedReader reader;
		
		String basePath = System.getProperty("user.dir");
		String classes_path = Paths.get(basePath, FunctionVector.LIBRARY_CLASSES_PATH).toString();
		
		try {
			reader = new BufferedReader(new FileReader(classes_path));
			String line = reader.readLine();
			while (line != null) {
				line = line.replace(".", "/");
				classes.add(line);
				line = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot read the library classes");
		}
	
		return classes;
	}
}
