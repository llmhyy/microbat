package microbat.model;

import microbat.model.trace.TraceNode;
import microbat.util.JavaUtil;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class Scope {
	private CompilationUnit cu;
	private int startLine;
	private int endLine;
	
	/**
	 * whether the scope contains some jump statments such as break, continue, 
	 * return, and throw.
	 */
	private boolean hasJumpStatement;

	private boolean isLoopScope;

	public CompilationUnit getCompilationUnit() {
		return cu;
	}

	public void setCompilationUnit(CompilationUnit cu) {
		this.cu = cu;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public boolean containsNodeScope(TraceNode node) {
		String nodeClassName = node.getClassCanonicalName();
		String scopeClassName = JavaUtil.getFullNameOfCompilationUnit(cu);

		if (nodeClassName.equals(scopeClassName)) {
			int line = node.getLineNumber();
			if (line >= startLine && line <= endLine) {
				return true;
			}
		}

		return false;
	}

	public boolean isLoopScope() {
		return isLoopScope;
	}

	public void setLoopScope(boolean isLoopScope) {
		this.isLoopScope = isLoopScope;
	}

	public void setHasJumpStatement(boolean hasJumpStatement) {
		this.hasJumpStatement = hasJumpStatement;
	}
	
	public boolean hasJumpStatement(){
		return this.hasJumpStatement;
	}

}
