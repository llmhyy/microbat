package microbat.evaluation.mutation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import microbat.util.JavaUtil;
import sav.strategies.dto.ClassLocation;

public class MutationPointChecker extends ASTVisitor{
	
	private CompilationUnit compilationUnit;
	private List<Integer> lines;
	
	private List<ClassLocation> mutationPoints = new ArrayList<>();
	
	public MutationPointChecker(CompilationUnit cu, List<Integer> lines){
		this.compilationUnit = cu;
		this.lines = lines;
	}
	
	public boolean visit(InfixExpression expr){
		checkMutationPoint(expr);
		return false;
	}
	
	public boolean visit(PostfixExpression expr){
		checkMutationPoint(expr);
		return false;
	}
	
	public boolean visit(PrefixExpression expr){
		checkMutationPoint(expr);
		return false;
	}
	
	private void checkMutationPoint(Expression expr){
		int lin = compilationUnit.getLineNumber(expr.getStartPosition());
		
		if(lines.contains(lin)){
			MethodDeclaration md = findMethod(expr);
			if(md != null){
				String methodName = md.getName().getIdentifier();
				String className = JavaUtil.getFullNameOfCompilationUnit(compilationUnit);
				ClassLocation location = new ClassLocation(className, methodName, lin);
				
				mutationPoints.add(location);
			}
		}
	}
	
	private MethodDeclaration findMethod(Expression expr){
		ASTNode parent = expr.getParent();
		while(parent != null && !(parent instanceof MethodDeclaration)){
			parent = parent.getParent();
		}
		
		if(parent instanceof MethodDeclaration){
			return (MethodDeclaration)parent;
		}
		
		return null;
	}

	public List<ClassLocation> getMutationPoints() {
		return mutationPoints;
	}

	public void setMutationPoints(List<ClassLocation> mutationPoints) {
		this.mutationPoints = mutationPoints;
	}
	
	
}
