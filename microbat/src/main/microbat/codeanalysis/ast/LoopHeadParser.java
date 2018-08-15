package microbat.codeanalysis.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.ui.JavaUI;

import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.SourceScope;
import microbat.util.JavaUtil;


public class LoopHeadParser extends ASTVisitor{
	CompilationUnit cu;
	int conditionLineNumber;
	BreakPoint point;
	
	private Statement conditionASTStatement; 
	
	private boolean isLoop = false;
	
	public LoopHeadParser(CompilationUnit cu, BreakPoint point){
		this.cu = cu;
		this.conditionLineNumber = point.getLineNumber();
		this.point = point;
	}
	
	public boolean visit(IfStatement statement){
		int line = cu.getLineNumber(statement.getStartPosition());
		if(line == conditionLineNumber){
			conditionASTStatement = statement;
		}
		return true;
	}
	
	public boolean visit(SwitchStatement statement){
		int line = cu.getLineNumber(statement.getStartPosition());
		if(line == conditionLineNumber){
			conditionASTStatement = statement;
		}
		return true;
	}
	
	public boolean visit(DoStatement statement){
		int line = cu.getLineNumber(statement.getExpression().getStartPosition());
		if(line == conditionLineNumber){
			conditionASTStatement = statement;
			isLoop = true;
			return false;
		}
		
		return true;
	}
	
	public boolean visit(EnhancedForStatement statement){
		int line = cu.getLineNumber(statement.getStartPosition());
		if(line == conditionLineNumber){
			conditionASTStatement = statement;
			isLoop = true;
			return false;
		}
		return true;
	}
	
	public boolean visit(ForStatement statement){
		int line = cu.getLineNumber(statement.getStartPosition());
		if(line == conditionLineNumber){
			conditionASTStatement = statement;
			isLoop = true;
			return false;
		}
		return true;
	}
	
	public boolean visit(WhileStatement statement){
		int line = cu.getLineNumber(statement.getExpression().getStartPosition());
		if(line == conditionLineNumber){
			conditionASTStatement = statement;
			isLoop = true;
			return false;
		}
		return true;
	}

	public boolean isLoop() {
		return isLoop;
	}

	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}
	
	public List<ClassLocation> extractLocation(){
		if(this.conditionASTStatement == null){
			return null;
		}
		
		int startLine = cu.getLineNumber(this.conditionASTStatement.getStartPosition());
		int endLine = cu.getLineNumber(this.conditionASTStatement.getStartPosition()+this.conditionASTStatement.getLength());
		
		List<ClassLocation> locationList = new ArrayList<>();
		for(int i=startLine; i<=endLine; i++){
			ClassLocation location = new ClassLocation(point.getClassCanonicalName(), null, i);
			locationList.add(location);
		}
		return locationList;
	}
	
	public SourceScope extractScope(){
		String className = JavaUtil.getFullNameOfCompilationUnit(cu);
		if(this.conditionASTStatement == null){
			return new SourceScope(className, 0, 0, false);
		}
		
		int startLine = cu.getLineNumber(this.conditionASTStatement.getStartPosition());
		int endLine = cu.getLineNumber(this.conditionASTStatement.getStartPosition()+this.conditionASTStatement.getLength());
		
		SourceScope ss = new SourceScope(className, startLine, endLine, this.isLoop);
		
		return ss;
	}
}
