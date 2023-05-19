package microbat.probability.SPP.vectorization.vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import microbat.model.value.VarValue;

public class VariableVector extends Vector {
	
	/*
	 * 8-dim: primitive type
	 * 1-dim: is array
	 * 1-dim: is local
	 * 1-dim: is static
	 * 1-dim: is field
	 * 1-dim: is library class
	 * 1-dim: computational cost
	 */
	public static final int PRIMITIVE_TYPE_COUNT = 8;
	public static final int DIMENSION = VariableVector.PRIMITIVE_TYPE_COUNT + 6;

	private static final List<String> PRIMITIVE_TYPES_1 = VariableVector.initPrimType_1();
	private static final List<String> PRIMITIVE_TYPES_2 = VariableVector.initPrimType_2();
	
	private static final int IS_ARRAY_IDX = VariableVector.PRIMITIVE_TYPE_COUNT + 0;
	private static final int IS_LOCAL_IDX = VariableVector.PRIMITIVE_TYPE_COUNT + 1;
	private static final int IS_STATIC_IDX = VariableVector.PRIMITIVE_TYPE_COUNT + 2;
	private static final int IS_INSTANCE_IDX = VariableVector.PRIMITIVE_TYPE_COUNT + 3;
	private static final int IS_RELIABLE_IDX = VariableVector.PRIMITIVE_TYPE_COUNT + 4;
	private static final int COST_IDX = VariableVector.PRIMITIVE_TYPE_COUNT + 5;

	public VariableVector() {
		super(VariableVector.DIMENSION);
	}
	
	public VariableVector(final VarValue var) {
		super(VariableVector.DIMENSION);
		
		String typeStr = var.getType();
		
		if (var.isArray()) {
			typeStr = typeStr.replace("[]", "");
			this.set(VariableVector.IS_ARRAY_IDX);
		}
		
		final int typeIdx = this.getTypeIdx(typeStr);
		if (typeIdx != -1) {
			this.set(typeIdx);
		}
		
		if (var.isLocalVariable()) this.set(VariableVector.IS_LOCAL_IDX);
		if (var.isField()) this.set(VariableVector.IS_INSTANCE_IDX);
		if (var.isStatic()) this.set(VariableVector.IS_STATIC_IDX);
		if (VariableVector.isReliableType(typeStr)) this.set(VariableVector.IS_RELIABLE_IDX);
		
		this.vector[VariableVector.COST_IDX] = (float) var.getComputationalCost();
	}

	private int getTypeIdx(final String typeStr) {
		int idx_1 = VariableVector.PRIMITIVE_TYPES_1.indexOf(typeStr);
		int idx_2 = VariableVector.PRIMITIVE_TYPES_2.indexOf(typeStr);
		return Math.max(idx_1, idx_2);
	}
	
	public static VariableVector[] constructVarVectors(final List<VarValue> vars, final int vectorCount) {
		VariableVector[] varVectors = new VariableVector[vectorCount];
		for (int idx=0; idx<vectorCount; idx++) {
			if (idx >= vars.size()) {
				varVectors[idx] = new VariableVector();
			} else {
				final VarValue var = vars.get(idx);
				varVectors[idx] = new VariableVector(var);
			}
		}
		return varVectors;
	}
	
	public static boolean isReliableType(final VarValue var) {
		String typeStr = var.getType();
		if (var.isArray()) {
			typeStr = typeStr.replace("[]", "");
		}
		return VariableVector.isReliableType(typeStr);
	}
	
	public static boolean isReliableType(final String type) {
		return LibraryClassDetector.isLibClass(type.replace(".", "/"));
	}
	
	private static List<String> initPrimType_1() {
		String[] types_array = {"int", "byte", "short", "long", "float", "double", "boolean", "char"};
		List<String> types = Arrays.asList(types_array);
		return types;
	}
	
	private static List<String> initPrimType_2() {
		String[] types_array = {"java.lang.Integer", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Boolean", "java.lang.Character"};
		List<String> types = Arrays.asList(types_array);
		return types;
	}

	
}
