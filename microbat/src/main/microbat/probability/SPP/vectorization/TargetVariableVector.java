package microbat.probability.SPP.vectorization;

import microbat.model.value.ArrayValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.StringValue;
import microbat.model.value.VarValue;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;

public class TargetVariableVector {
	
	// Primitive type
	protected final static int isByteIdx = 0;
	protected final static int isShortIdx = TargetVariableVector.isByteIdx+1;
	protected final static int isIntIdx = TargetVariableVector.isShortIdx+1;
	protected final static int isLongIdx = TargetVariableVector.isIntIdx+1;
	protected final static int isFloatIdx = TargetVariableVector.isLongIdx+1;
	protected final static int isDoubleIdx = TargetVariableVector.isFloatIdx+1;
	protected final static int isCharIdx = TargetVariableVector.isDoubleIdx+1;
	protected final static int isBooleanIdx = TargetVariableVector.isCharIdx+1;
	
	// Special Type
	protected final static int isArrayIdx = TargetVariableVector.isBooleanIdx+1;
	protected final static int isStringIdx = TargetVariableVector.isArrayIdx+1;
	protected final static int isNullIdx = TargetVariableVector.isStringIdx+1;
	protected final static int isThisIdx = TargetVariableVector.isNullIdx+1;
	
	// Scope
	protected final static int isLocalIdx = TargetVariableVector.isNullIdx+1;
	protected final static int isInstanceIdx = TargetVariableVector.isLocalIdx+1;
	protected final static int isStaticIdx = TargetVariableVector.isInstanceIdx+1;
	
	// Modifier
//	protected final static int isPublic = TargetVariableVector.isStaticIdx+1;
//	protected final static int isProtected = TargetVariableVector.isPublic+1;
//	protected final static int isPrivate = TargetVariableVector.isProtected+1;
	
	protected final boolean[] featureArray = new boolean[15];
	
	protected TargetVariableVector() {
		for(int i=0; i<this.featureArray.length; ++i) {
			this.featureArray[i] = false;
		}
	}
	
	public TargetVariableVector(final VarValue var) {
		
	}
	
	public boolean[] getFeatureArray() {
		return this.featureArray;
	}
	
	protected void setFeature(final int idx, boolean feature) {
		this.featureArray[idx] = feature;
	}
	
	protected boolean isArray(final VarValue var) {
		return var instanceof ArrayValue;
	}
	
	protected boolean isString(final VarValue var) {
		return var instanceof StringValue;
	}
	
	protected boolean isNull(final VarValue var) {
		if (var instanceof ReferenceValue) {
			ReferenceValue refVar = (ReferenceValue) var;
			return refVar.isNull();
		} else {
			return false;
		}
	}
	
	protected boolean isLocalVar(final VarValue var) {
		return var.isLocalVariable();
	}
	
	protected boolean isInstanceVar(final VarValue var) {
		return var.isField();
	}
	
	protected boolean isStaticVar(final VarValue var) {
		return var.isStatic();
	}
	
	protected boolean isThisVar(final VarValue var) {
		return var.getVarName() == "this";
	}
	
	protected boolean isPrivate(final VarValue var) {
		return false;
	}
	
	protected boolean isProtected(final VarValue var) {
		return false;
	}
	
	protected boolean isPublic(final VarValue var) {
		return false;
	}
	
	protected boolean isStatic(final VarValue var) {
		return false;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (otherObj instanceof TargetVariableVector) {
			TargetVariableVector otherVec = (TargetVariableVector) otherObj;
			if (this.featureArray.length != otherVec.featureArray.length) {
				return false;
			}
			
			for (int idx=0; idx<this.featureArray.length; idx++) {
				boolean thisFeature = this.featureArray[idx];
				boolean otherFeature = otherVec.featureArray[idx];
				if( thisFeature != otherFeature) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.featureArray[0]?"1":"0");
		for(int idx=0; idx<this.featureArray.length; idx++) {
			strBuilder.append(",");
			boolean feature = this.featureArray[idx];
			strBuilder.append(feature ? "1" : "0");
		}
		return strBuilder.toString();
	}
}
