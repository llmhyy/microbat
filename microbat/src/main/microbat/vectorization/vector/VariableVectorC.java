package microbat.vectorization.vector;

import microbat.model.value.VarValue;
import microbat.model.variable.ConditionVar;

public class VariableVectorC extends VariableVector {
	
	public static final int DIMENSION = VariableVector.DIMENSION +1;
	public static final int COST_IDX = VariableVectorC.DIMENSION-1;
	
	public VariableVectorC() {
		super(VariableVectorC.DIMENSION);
	}
	
	public VariableVectorC(final int size) {
		super(size);
	}
	
	public VariableVectorC(final VarValue var) {
		super(VariableVectorC.DIMENSION);
		
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
//		if (VariableVector.isReliableType(typeStr)) this.set(VariableVector.IS_RELIABLE_IDX);
//		
		this.vector[VariableVectorC.COST_IDX] = (float) var.getSuspiciousness();
		if (var.getVarID().startsWith(ConditionVar.CONDITION_RESULT_ID)) this.set(VariableVector.IS_CONDITION_RESULT_IDX);
	
	}
}
