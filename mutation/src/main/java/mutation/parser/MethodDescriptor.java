package mutation.parser;

import japa.parser.ast.expr.NameExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by hoangtung on 3/31/15.
 */
public class MethodDescriptor {
	private ClassDescriptor classDescriptor;
	private int modifier;
	private String name;
	private Object returnType;
	private List<VariableDescriptor> parameters;
	// start, end,
	private List<LocalVariable> localVars;
	private int openScopeIdx;
	private int beginLine;
	private int endLine;

	public MethodDescriptor(int beginLine, int endLine) {
		parameters = new ArrayList<VariableDescriptor>();
		localVars = new ArrayList<LocalVariable>();
		this.beginLine = beginLine;
		this.endLine = endLine;
	}

	public void openScope(int beginLine) {
		localVars.add(new LocalVariable(beginLine, -1,
				new HashMap<String, VariableDescriptor>()));
		openScopeIdx = localVars.size() - 1;
	}

	public void closeScope(int endLine) {
		// stupid way of closing scope
		localVars.get(openScopeIdx).setEndLine(endLine);
		ListIterator<LocalVariable> it = localVars.listIterator(openScopeIdx);

		while (it.hasPrevious()) {
			LocalVariable scope = it.previous();
			--openScopeIdx;
			if (scope.getEndLine() < 0) {
				break;
			}
		}
	}

	public boolean containsLine(int lineNumber){
		return beginLine <= lineNumber && lineNumber <= endLine;
	}
	
	public VariableDescriptor getVarFromName(String name, int beginLine,
			int endLine) {
		//TODO LLT;
		return null;
		// return null;
	}

	public VariableDescriptor getVarFromName(NameExpr name) {
		//TODO LLT;
		return null;
	}

	public void addLocalVar(VariableDescriptor variableDescriptor) {
		localVars.get(openScopeIdx).put(variableDescriptor.getName(),
				variableDescriptor);
	}

	public void addLocalVars(List<VariableDescriptor> vList) {
		for (VariableDescriptor var : vList)
			localVars.get(openScopeIdx).put(var.getName(), var);
	}

	public void addParamter(VariableDescriptor vp) {
		this.parameters.add(vp);
	}

	public ClassDescriptor getClassDescriptor() {
		return classDescriptor;
	}

	public int getModifier() {
		return modifier;
	}

	public String getName() {
		return name;
	}

	public Object getReturnType() {
		return returnType;
	}

	public List<VariableDescriptor> getParameters() {
		return parameters;
	}

	public List<LocalVariable> getLocalVars() {
		return localVars;
	}

	public int getOpenScopeIdx() {
		return openScopeIdx;
	}

	public void setClassDescriptor(ClassDescriptor classDescriptor) {
		this.classDescriptor = classDescriptor;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReturnType(Object returnType) {
		this.returnType = returnType;
	}

	public void setParameters(List<VariableDescriptor> parameters) {
		this.parameters = parameters;
	}

	public void setLocalVars(List<LocalVariable> localVars) {
		this.localVars = localVars;
	}

	public void setOpenScopeIdx(int openScopeIdx) {
		this.openScopeIdx = openScopeIdx;
	}

	@Override
	public String toString() {
		return "MethodDescriptor [name=" + name + ", returnType=" + returnType
				+ ", parameters=" + parameters + ", localVars=" + localVars
				+ "]";
	}
	
}
