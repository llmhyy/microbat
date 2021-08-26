package microbat.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import microbat.model.variable.Variable;
import sav.common.core.utils.ClassUtils;

public class BreakPoint extends ClassLocation {
	private List<Variable> readVariables = new ArrayList<>();
	private List<Variable> writtenVariables = new ArrayList<>();
	
	private List<Variable> allVisibleVariables = new ArrayList<>();
	
	private boolean isReturnStatement;
	
	private boolean isStartOfClass = false;
	
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
	private List<ClassLocation> targets = new ArrayList<>();
	
	
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
		point.setMethodSign(location.getMethodSign());
		point.setAllVisibleVariables(allVisibleVariables);
		point.setControlScope(controlScope);
		point.setConditional(isConditional);
		point.setReturnStatement(isReturnStatement);
		point.setLoopScope(loopScope);
		point.setTargets(targets);
		point.setReadVariables(readVariables);
		point.setWrittenVariables(readVariables);
		return point;
	}
	
	public void addReadVariable(Variable var){
		if(!this.readVariables.contains(var)){
			this.readVariables.add(var);			
		}
	}
	
	public void addWrittenVariable(Variable var){
		if(!this.writtenVariables.contains(var)){
			this.writtenVariables.add(var);
		}
	}
	
	public List<Variable> getAllVisibleVariables() {
		return allVisibleVariables;
	}

	public void setAllVisibleVariables(List<Variable> allVisibleVariables) {
		this.allVisibleVariables = allVisibleVariables;
	}

	public List<Variable> getReadVariables() {
		return readVariables;
	}

	public void setReadVariables(List<Variable> readVariables) {
		this.readVariables = readVariables;
	}

	public List<Variable> getWrittenVariables() {
		return writtenVariables;
	}

	public void setWrittenVariables(List<Variable> writtenVariables) {
		this.writtenVariables = writtenVariables;
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

	public List<ClassLocation> getTargets() {
		return targets;
	}

	public void setTargets(List<ClassLocation> targets) {
		this.targets = targets;
	}
	
	public void addTarget(ClassLocation target){
		if(!this.targets.contains(target)){
			this.targets.add(target);			
		}
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

	public String getShortMethodSignature() {
		String methodSig = this.methodSign;
		String shortSig = methodSig.substring(methodSig.indexOf("#")+1, methodSig.length());
		
		return shortSig;
	}
	
	private boolean isSourceVersion;
	

	public boolean isStartOfClass() {
		return isStartOfClass;
	}

	public void setStartOfClass(boolean isStartOfClass) {
		this.isStartOfClass = isStartOfClass;
	}

	public String getPackageName() {
		if(this.classCanonicalName.contains(".")){
			return this.classCanonicalName.substring(0, this.classCanonicalName.lastIndexOf("."));
		}
		else{
			return "";
		}
	}

	public boolean isSourceVersion() {
		return isSourceVersion;
	}

	public void setSourceVersion(boolean isSourceVersion) {
		this.isSourceVersion = isSourceVersion;
	}

	public boolean isBranch() {
		return isBranch;
	}

	public void setBranch(boolean isBranch) {
		this.isBranch = isBranch;
	}
}
