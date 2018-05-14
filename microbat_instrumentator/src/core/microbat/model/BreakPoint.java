package microbat.model;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import sav.common.core.utils.ClassUtils;

public class BreakPoint extends ClassLocation {
	private boolean isReturnStatement;
	
	private boolean isConditional;
	private boolean isBranch;
	
	/**
	 * The reason to differentiate control scope and loop scope is that
	 * (1) control scope include more than a code block, e.g., a statement outside a block
	 * can be control dependent on a statement inside a block.
	 * (2) in contrast, loop scope can only include the statements inside a code block.
	 */
	private ControlScope controlScope;
	private SourceScope loopScope;
	private String declaringCompilationUnitName;
	
	public BreakPoint(String className, String methodSinature, int linNum){
		super(className, methodSinature, linNum);
		this.declaringCompilationUnitName = ClassUtils.getCompilationUnitForSimpleCase(className);
	}
	
	public BreakPoint(String className, String declaringCompilationUnitName, String methodSign, int lineNo) {
		super(className, methodSign, lineNo);
		this.declaringCompilationUnitName = declaringCompilationUnitName;
	}
	
	public Object clone(){
		ClassLocation location = (ClassLocation) super.clone();
		BreakPoint point = new BreakPoint(location.getClassCanonicalName(), declaringCompilationUnitName, lineNo);
		point.setControlScope(controlScope);
		point.setConditional(isConditional);
		point.setReturnStatement(isReturnStatement);
		point.setLoopScope(loopScope);
		return point;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		if(obj instanceof ClassLocation){
			ClassLocation other = (ClassLocation) obj;
			
			return classCanonicalName.equals(other.getClassCanonicalName())
					&& lineNo == other.getLineNumber();			
		}
		
		if(obj instanceof BreakPoint){
			BreakPoint other = (BreakPoint) obj;
			return declaringCompilationUnitName.equals(other.getDeclaringCompilationUnitName())
					&& lineNo == other.getLineNumber();			
		}
		
		return false;
	}

	public boolean valid() {
		return lineNo > 0;
	}
	
	public String getMethodSign() {
//		if(methodSign == null){
//			System.err.println("missing method name!");
//		}
		return methodSign;
	}
	

	public List<Integer> getOrgLineNos() {
		return Arrays.asList(lineNo);
	}

	@Override
	public String toString() {
		return "BreakPoint [classCanonicalName=" + classCanonicalName
				 + ", lineNo=" + lineNo + ", methodName=" + methodSign
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((classCanonicalName == null) ? 0 : classCanonicalName
						.hashCode());
		result = prime * result + lineNo;
		return result;
	}

	public boolean isReturnStatement() {
		return isReturnStatement;
	}

	public void setReturnStatement(boolean isReturnStatement) {
		this.isReturnStatement = isReturnStatement;
	}
	
	public String getDeclaringCompilationUnitName(){
		return this.declaringCompilationUnitName;
	}
	
	public String getClassCanonicalName(){
		return super.getClassCanonicalName();
	}

	public void setConditional(boolean isConditional) {
		this.isConditional = isConditional;
	}
	
	public boolean isConditional(){
		return this.isConditional;
	}

	public ControlScope getControlScope() {
		return controlScope;
	}

	public void setControlScope(ControlScope conditionScope) {
		this.controlScope = conditionScope;
	}

	public void mergeControlScope(ControlScope locationScope) {
		if(this.controlScope == null){
			this.controlScope = locationScope;
		}
		else{
			for(ClassLocation location: locationScope.getRangeList()){
				if(this.controlScope instanceof ControlScope){
					ControlScope thisScope = (ControlScope)this.controlScope;
					if(!thisScope.containLocation(location)){
						thisScope.addLocation(location);
					}
				}
			}
		}
	}

	public SourceScope getLoopScope() {
		return loopScope;
	}

	public void setLoopScope(SourceScope loopScope) {
		this.loopScope = loopScope;
	}

	public void setDeclaringCompilationUnitName(String declaringCompilationUnitName) {
		this.declaringCompilationUnitName = declaringCompilationUnitName;
	}

	public String getShortMethodSignature() {
		String methodSig = this.methodSign;
		String shortSig = methodSig.substring(methodSig.indexOf("#")+1, methodSig.length());
		
		return shortSig;
	}
	
	public boolean isSourceVersion() {
		String flag = File.separator + "bug" + File.separator;
		return this.getFullJavaFilePath().contains(flag);
	}

	public boolean isBranch() {
		return isBranch;
	}

	public void setBranch(boolean isBranch) {
		this.isBranch = isBranch;
	}
}
