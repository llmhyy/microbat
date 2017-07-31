package microbat.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import microbat.model.variable.Variable;
import sav.common.core.utils.CollectionUtils;

public class BreakPoint extends ClassLocation {
	protected List<Variable> vars; // to keep order
	private int charStart;
	private int charEnd;
	
	private List<Variable> readVariables = new ArrayList<>();
	private List<Variable> writtenVariables = new ArrayList<>();
	
	private List<Variable> allVisibleVariables = new ArrayList<>();
	
	private boolean isReturnStatement;
	
	private boolean isConditional;
	private ControlScope controlScope;
	private SourceScope loopScope;
	private List<ClassLocation> targets = new ArrayList<>();
	private String declaringCompilationUnitName;
	
	public BreakPoint(String className, String declaringCompilationUnitName, int linNum){
		super(className, null, linNum);
		vars = new ArrayList<Variable>();
		this.declaringCompilationUnitName = declaringCompilationUnitName;
	}
	
	public BreakPoint(String className, String declaringCompilationUnitName, String methodSign, int lineNo) {
		super(className, methodSign, lineNo);
		vars = new ArrayList<Variable>();
		this.declaringCompilationUnitName = declaringCompilationUnitName;
	}
	
	public BreakPoint(String className, String declaringCompilationUnitName, int lineNo, Variable... newVars) {
		this(className, null, lineNo);
		if (newVars != null) {
			addVars(newVars);
		}
		this.declaringCompilationUnitName = declaringCompilationUnitName;
	}
	
	public Object clone(){
		ClassLocation location = (ClassLocation) super.clone();
		BreakPoint point = new BreakPoint(location.getClassCanonicalName(), declaringCompilationUnitName, lineNo);
		point.setAllVisibleVariables(allVisibleVariables);
		point.setCharEnd(charEnd);
		point.setCharStart(charStart);
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

	public void addVars(Variable... newVars) {
		for (Variable newVar : newVars) {
			vars.add(newVar);
		}
	}
	
	public void addVars(List<Variable> newVars) {
		for (Variable newVar : newVars) {
			CollectionUtils.addIfNotNullNotExist(vars, newVar);
			
		}
	}
	
	public List<Variable> getVars() {
		return vars;
	}

	public void setVars(List<Variable> vars) {
		this.vars = vars;
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
	
	public int getCharStart() {
		return charStart;
	}

	public void setCharStart(int charStart) {
		this.charStart = charStart;
	}

	public int getCharEnd() {
		return charEnd;
	}

	public void setCharEnd(int charEnd) {
		this.charEnd = charEnd;
	}
	
	public List<Integer> getOrgLineNos() {
		return Arrays.asList(lineNo);
	}

	@Override
	public String toString() {
		return "BreakPoint [classCanonicalName=" + classCanonicalName
				+ ", methodName=" + methodSign + ", lineNo=" + lineNo
				+ ", vars=" + vars + ", charStart=" + charStart + ", charEnd="
				+ charEnd + "]";
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

	public Scope getControlScope() {
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
					if(!thisScope.containsLocation(location)){
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

	public boolean isSourceVersion() {
		String flag = File.separator + "bug" + File.separator;
		return this.getFullJavaFilePath().contains(flag);
	}
}
