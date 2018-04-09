package microbat.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class MinimumASTNodeFinder extends ASTVisitor{
	private int line;
	private CompilationUnit cu;
	
	private int startLine = -1;
	private int endLine = -1;
	
	private ASTNode minimumNode = null;

	public MinimumASTNodeFinder(int line, CompilationUnit cu) {
		super();
		this.line = line;
		this.cu = cu;
	}

	@Override
	public void preVisit(ASTNode node) {
		
		int start = cu.getLineNumber(node.getStartPosition());
		int end = cu.getLineNumber(node.getStartPosition()+node.getLength());
		
		if(start<=line && line<=end){
			if(getMinimumNode() == null){
				startLine = start;
				endLine = end;
				setMinimumNode(node);
			}
			else{
				boolean flag = false;
				
				if(startLine<start && end<endLine && startLine!=line){
					startLine = start;
					endLine = end;
					flag = true;
				}
				
				if(flag){
					setMinimumNode(node);						
				}
			}
		}
	}

	public ASTNode getMinimumNode() {
		return minimumNode;
	}

	public void setMinimumNode(ASTNode minimumNode) {
		this.minimumNode = minimumNode;
	}
}