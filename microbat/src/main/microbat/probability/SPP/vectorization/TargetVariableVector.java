package microbat.probability.SPP.vectorization;

import microbat.model.value.VarValue;

public class TargetVariableVector {
	
	
	public final static int isArrayIdx = 0;
	public final static int isStringIdx = 1;
	public final static int isNullIdx = 2;
	
	protected final boolean[] typeVector = new boolean[18];
	
	public TargetVariableVector(final VarValue var) {
		
	}
	
	protected boolean isArray(final VarValue var) {
		return false;
	}
	
	protected boolean isString(final VarValue var) {
		return false;
	}
	
	protected boolean isNull(final VarValue var) {
		return false;
	}
	
	protected boolean isLocalVar(final VarValue var) {
		return false;
	}
	
	protected boolean isInstanceVar(final VarValue var) {
		return false;
	}
	
	protected boolean isStaticVar(final VarValue var) {
		return false;
	}
	
	protected boolean isThisVar(final VarValue var) {
		return false;
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
		return false;
	}
	
	@Override
	public String toString() {
		return null;
	}
}
