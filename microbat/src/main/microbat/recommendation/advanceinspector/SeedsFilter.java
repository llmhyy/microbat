package microbat.recommendation.advanceinspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import microbat.model.value.VarValue;
import microbat.util.JavaUtil;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;

public class SeedsFilter {
	
	class AssignmentChecker extends ASTVisitor{
		List<Unit> potentialSeeds;
		List<Unit> visitedSeeds = new ArrayList<>();
		
		CompilationUnit cu;
		
		public AssignmentChecker(List<Unit> potentialSeeds, CompilationUnit cu){
			this.potentialSeeds = potentialSeeds;
			this.cu = cu;
		}
		
		@Override
		public boolean visit(ExpressionStatement exprStat){
			
			Expression expr = exprStat.getExpression();
			if(expr instanceof Assignment){
				Assignment assign = (Assignment)expr;
				int line = this.cu.getLineNumber(assign.getStartPosition());
				
				List<Unit> correspondingUnits = findUnits(potentialSeeds, line);
				
				for(Unit unit: correspondingUnits){
					potentialSeeds.remove(unit);
					visitedSeeds.add(unit);
				}
			}
			
			
			return false;
		}
		
		@Override
		public boolean visit(VariableDeclarationStatement exprStat){
			
			Object obj = exprStat.fragments().get(0);
			if(obj instanceof VariableDeclarationFragment){
				VariableDeclarationFragment fragment = (VariableDeclarationFragment)obj;
				int line = this.cu.getLineNumber(fragment.getStartPosition());
				
				List<Unit> correspondingUnits = findUnits(potentialSeeds, line);
				
				for(Unit unit: correspondingUnits){
					potentialSeeds.remove(unit);
					visitedSeeds.add(unit);
				}
			}
			return false;
		}

		private List<Unit> findUnits(List<Unit> potentialSeeds2, int line) {
			List<Unit> filteredList = new ArrayList<>();
			for(Unit unit: potentialSeeds2){
				for(Tag tag: unit.getTags()){
					if(tag instanceof LineNumberTag){
						LineNumberTag lTag = (LineNumberTag)tag;
						if(lTag.getLineNumber()==line){
							filteredList.add(unit);
							break;
						}
					}
				}
			}
			
			System.currentTimeMillis();
			
			return filteredList;
		}
	}
	
	public Map<String, List<Unit>> filter(Map<String, List<Unit>> seedMap, VarValue var){
		Map<String, List<Unit>> validSeeds = new HashMap<>();
		
		for(String className: seedMap.keySet()){
			List<Unit> potentialSeeds = seedMap.get(className);
			CompilationUnit cu = JavaUtil.findCompilationUnitInProject(className, null);
			AssignmentChecker checker = new AssignmentChecker(potentialSeeds, cu);
			cu.accept(checker);
			validSeeds.put(className, checker.visitedSeeds);
		}
		
		return validSeeds;
	}
}
