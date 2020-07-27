package microbat.model;

import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.RecordType;
import microbat.model.trace.TraceNode;

@RecordType
public class SourceScope implements Scope{
	@Attribute
	private String className;
	@Attribute
	private int startLine;
	@Attribute
	private int endLine;
	
	public SourceScope(String className, int startLine, int endLine, boolean isLoopScope) {
		super();
		this.className = className;
		this.startLine = startLine;
		this.endLine = endLine;
		this.isLoopScope = isLoopScope;
	}

	public SourceScope(String className, int start, int end) {
		super();
		this.className = className;
		this.startLine = start;
		this.endLine = end;
	}

	/**
	 * whether the scope contains some jump statements such as break, continue, 
	 * return, and throw.
	 */
	private boolean hasJumpStatement;

	private boolean isLoopScope;

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
		return containLocation(node.getBreakPoint());
	}

	public boolean isLoop() {
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

	@Override
	public boolean containLocation(ClassLocation location) {
		String nodeClassName = location.getDeclaringCompilationUnitName();

		if (nodeClassName.equals(className)) {
			int line = location.getLineNumber();
			if (line >= startLine && line <= endLine) {
				return true;
			}
		}

		return false;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
