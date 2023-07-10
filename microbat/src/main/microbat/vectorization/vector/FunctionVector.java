package microbat.vectorization.vector;

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
	
	/*
	 * Function vector structure:
	 * 
	 * 5-dim: one vector of invoke type
	 * 1-dim: Indicate this function is called by library object
	 * 20-dim: histogram of input type
	 * 	8-dim for primitive type
	 * 	1-dim for library object4
	 * 	1-dim for self-defined object
	 * 	10-dim array version for the above feature
	 * 21-dim: one hot vector of output type. Types are same as input type with additional void type
	 * 
	 * 
	 */
	
	public static final int INVOKE_TYPE_COUNT = 5;
	public static final int DIMENSION = FunctionVector.INVOKE_TYPE_COUNT + 1 +
										InputParameterVector.DIMENSION + 
										OutputParameterVector.DIMENSION;
	
	private final InputParameterVector inputParamTypeVector;
	private final OutputParameterVector outputParamTypeVector;
	
	private static final int DYNAMIC_IDX = 0;
	private static final int INTERFACE_IDX = 1;
	private static final int SPECIAL_IDX = 2;
	private static final int STATIC_IDX = 3;
	private static final int VIRTUAL_IDX = 4;
	private static final int LIBRARY_FUNC_IDX = 5;
	
	public FunctionVector() {
		super(FunctionVector.DIMENSION);
		this.inputParamTypeVector = new InputParameterVector();
		this.outputParamTypeVector = new OutputParameterVector();
	}
	
	public FunctionVector(final ByteCode byteCode, final String funcSign) {
		super(FunctionVector.INVOKE_TYPE_COUNT+1);
		
		// Check invoke type
		final short optCode = byteCode.getOpcode();
		if (this.isInvokeDynamic(optCode)) this.set(FunctionVector.DYNAMIC_IDX);
		if (this.isInvokeInterface(optCode)) this.set(FunctionVector.INTERFACE_IDX);
		if (this.isInvokeSpecial(optCode)) this.set(FunctionVector.SPECIAL_IDX);
		if (this.isInvokeStatic(optCode)) this.set(FunctionVector.STATIC_IDX);
		if (this.isInvokeVirtual(optCode)) this.set(FunctionVector.VIRTUAL_IDX);
		
		final String classname = this.extractClassName(funcSign).replace(".", "/");
		if (LibraryClassDetector.isLibClass(classname)) this.set(LIBRARY_FUNC_IDX);
		
		final String inputTypeDescriptors = this.extractInputTypeDescriptors(funcSign);
		this.inputParamTypeVector = new InputParameterVector(inputTypeDescriptors);
		this.vector = ArrayUtils.addAll(this.vector, this.inputParamTypeVector.getVector());
		
		final String returnTypeDescriptors = this.extractReturnTypeDescriptors(funcSign);
		this.outputParamTypeVector = new OutputParameterVector(returnTypeDescriptors);
		this.vector = ArrayUtils.addAll(this.vector, this.outputParamTypeVector.getVector());
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
		
		// Need to check the matched node as sometimes it may contains part 
		// of the byte code or function signature
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
//			System.out.println("Don't have matched node");
		}


		
		if (funcSigns.size() != invokeCodes.size()) {
			for(int idx=0; idx<funcVectors.length; idx++) {
				funcVectors[idx] = new FunctionVector();
			}
			return funcVectors;
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
}
