package microbat.vectorization.vector;

import java.util.Arrays;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.trace.TraceNode;
import java.util.ArrayList;
import java.util.List;

public class ClassificationVector extends Vector {
	
	public static final int DIMENSION = 3;
	
	private static final List<OpcodeType> mathOpcodes = ClassificationVector.initMathOpcodes();
	private static final List<OpcodeType> invokeOpcodes = ClassificationVector.initInvokeOpcodes();
	private static final List<OpcodeType> initOpcodes = ClassificationVector.initInitOpcodes();
	private static final List<OpcodeType> returnOpcodes = ClassificationVector.initReturnOpcodes();
	private static final List<OpcodeType> bitOpcodes = ClassificationVector.initBitOpcodes();
	private static final List<OpcodeType> loadOpcodes = ClassificationVector.initLoadOpcodes();
	private static final List<OpcodeType> storeOpcodes = ClassificationVector.initStoreOpcodes();
	private static final List<OpcodeType> compareOpcodes = ClassificationVector.initCompareOpcodes();
	
	private static final int NORMAL_IDX = 0;
	private static final int CONDITION_DIX = 1;
	private static final int INVOKE_IDX = 2;
//	private static final int MATH_IDX = 0;
//	private static final int CALL_METHOD_IDX = 1;
//	private static final int CONDITION_IDX = 2;
//	private static final int INIT_IDX = 3;
//	private static final int RETURN_IDX = 4;
//	private static final int BIT_IDX = 5;
//	private static final int LOAD_IDX = 6;
//	private static final int STORE_IDX = 7;
	
	public ClassificationVector(final TraceNode node) {
		super(ClassificationVector.DIMENSION);
		ByteCodeList byteCodes = new ByteCodeList(node.getBytecode());
		if (!node.isConditional() && !this.isInvokeOpcodes(byteCodes)) {
			this.set(NORMAL_IDX);
		} else {
			if (node.isConditional()) {
				this.set(CONDITION_DIX);
			}
			if (this.isInvokeOpcodes(byteCodes)) {
				this.set(INVOKE_IDX);
			}
		}
//		if (this.isMathOpt(byteCodes)) this.set(ClassificationVector.MATH_IDX);
//		if (this.isInvokeOpcodes(byteCodes)) this.set(ClassificationVector.CALL_METHOD_IDX);
//		if (this.isCompare(byteCodes)) this.set(ClassificationVector.CONDITION_IDX);
//		if (this.isInitOpcodes(byteCodes)) this.set(ClassificationVector.INIT_IDX);
//		if (this.isReturnOpcodes(byteCodes)) this.set(ClassificationVector.RETURN_IDX);
//		if (this.isBitOpcodes(byteCodes)) this.set(ClassificationVector.BIT_IDX);
//		if (this.isLoadOpcodes(byteCodes)) this.set(ClassificationVector.LOAD_IDX);
//		if (this.isStoreOpcodes(byteCodes)) this.set(ClassificationVector.STORE_IDX);
	}
	
	public ClassificationVector(final float[] vector) {
		super(vector);
	}
	
	private boolean isCompare(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.compareOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isMathOpt(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.mathOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isInvokeOpcodes(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.invokeOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isInitOpcodes(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.initOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isReturnOpcodes(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.returnOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isBitOpcodes(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.bitOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isLoadOpcodes(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.loadOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isStoreOpcodes(final ByteCodeList byteCodes) {
		for (ByteCode byteCode : byteCodes) {
			if (ClassificationVector.storeOpcodes.contains(byteCode.getOpcodeType())) {
				return true;
			}
		}
		return false;
	}
	
	private static List<OpcodeType> initMathOpcodes() {
		List<OpcodeType> mathOpcodes = new ArrayList<>();
		mathOpcodes.add(OpcodeType.ADD);
		mathOpcodes.add(OpcodeType.SUB);
		mathOpcodes.add(OpcodeType.MUL);
		mathOpcodes.add(OpcodeType.DIV);
		mathOpcodes.add(OpcodeType.REM);
		mathOpcodes.add(OpcodeType.NEG);
		mathOpcodes.add(OpcodeType.INCREMENT);
		return mathOpcodes;
	}
	
	private static List<OpcodeType> initInvokeOpcodes() {
		List<OpcodeType> invokeOpcodes = new ArrayList<>();
		invokeOpcodes.add(OpcodeType.INVOKE);
		return invokeOpcodes;
	}

	private static List<OpcodeType> initInitOpcodes() {
		List<OpcodeType> initOpcodes = new ArrayList<>();
		initOpcodes.add(OpcodeType.NEW);
		return initOpcodes;
	}
	
	private static List<OpcodeType> initReturnOpcodes() {
		List<OpcodeType> returnOpcodes = new ArrayList<>();
		returnOpcodes.add(OpcodeType.RETURN);
		return returnOpcodes;
	}
	
	private static List<OpcodeType> initBitOpcodes() {
		List<OpcodeType> bitOpcodes = new ArrayList<>();
		bitOpcodes.add(OpcodeType.AND);
		bitOpcodes.add(OpcodeType.OR);
		bitOpcodes.add(OpcodeType.XOR);
		bitOpcodes.add(OpcodeType.LEFT_SHIFT);
		bitOpcodes.add(OpcodeType.RIGHT_SHIFT);
		return bitOpcodes;
	}
	
	private static List<OpcodeType> initLoadOpcodes() {
		List<OpcodeType> loadOpcodes = new ArrayList<>();
		loadOpcodes.add(OpcodeType.LOAD_CONSTANT);
		loadOpcodes.add(OpcodeType.LOAD_FROM_ARRAY);
		loadOpcodes.add(OpcodeType.LOAD_VARIABLE);
		loadOpcodes.add(OpcodeType.GET_STATIC_FIELD);
		loadOpcodes.add(OpcodeType.GET_FIELD);
		return loadOpcodes;
	}
	
	private static List<OpcodeType> initStoreOpcodes() {
		List<OpcodeType> storeOpcodes = new ArrayList<>();
		storeOpcodes.add(OpcodeType.STORE_VARIABLE);
		
		storeOpcodes.add(OpcodeType.STORE_INTO_ARRAY);
		storeOpcodes.add(OpcodeType.PUT_STATIC_FIELD);
		storeOpcodes.add(OpcodeType.PUT_FIELD);
		return storeOpcodes;
	}
	
	private static List<OpcodeType> initCompareOpcodes() {
		List<OpcodeType> storeOpcodes = new ArrayList<>();
		storeOpcodes.add(OpcodeType.IF_EQUALS);
		storeOpcodes.add(OpcodeType.IF_NOT_EQUALS);
		storeOpcodes.add(OpcodeType.IF_GREATER);
		storeOpcodes.add(OpcodeType.IF_LESS);
		storeOpcodes.add(OpcodeType.IF_GREATER_EQUALS);
		storeOpcodes.add(OpcodeType.IF_LESS_EQUALS);
		storeOpcodes.add(OpcodeType.COMPARE);
		storeOpcodes.add(OpcodeType.IF_NULL);
		storeOpcodes.add(OpcodeType.IF_NOT_NULL);
		return storeOpcodes;
	}
	
}
