package microbat.mutation.mutation;

import java.util.List;

import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import mutation.mutator.MutationVisitor;
import mutation.mutator.mapping.MutationMap;
import mutation.parser.ClassAnalyzer;
import sav.common.core.utils.CollectionUtils;

/**
 * 
 * @author LLT
 *
 */
public class TraceMutationVisitor extends MutationVisitor {
	
	public TraceMutationVisitor() {
		super();
	}

	public TraceMutationVisitor(MutationMap mutationMap, ClassAnalyzer classAnalyzer) {
		super(mutationMap, classAnalyzer);
	}
	
	@Override
	public boolean mutate(AssignExpr n) {
		MutationNode muNode = newNode(n);
		muNode.add(new EmptyStmt(), MutationTypes.REMOVE_ASSIGNMENT);
		return super.mutate(n);
	}

	@Override
	public boolean mutate(IfStmt n) {
		IfStmt ifStmt = (IfStmt) nodeCloner.visit(n, null);
		// remove Stmt Block Which Has Return Stmt 
		if (isEmptyOrContainReturnStmt(ifStmt.getThenStmt())) {
			ifStmt.setThenStmt(null);
		}
		if (isEmptyOrContainReturnStmt(ifStmt.getElseStmt())) {
			ifStmt.setElseStmt(null);
		}
		MutationNode muNode = newNode(n);
		if (ifStmt.getThenStmt() == null && ifStmt.getElseStmt() == null) {
			/* empty if or if which has return stmt in its stmt block */
			muNode.add(new EmptyStmt(), MutationTypes.REMOVE_IF_BLOCK);
		} else {
			if (ifStmt.getThenStmt() != null) {
				Node newNode = ifStmt.getThenStmt().accept(nodeCloner, null);
				muNode.add(newNode, MutationTypes.REMOVE_IF_CONDITION);
			} 
			if (ifStmt.getElseStmt() != null) {
				Node newNode = ifStmt.getElseStmt().accept(nodeCloner, null);
				muNode.add(newNode, MutationTypes.REMOVE_IF_CONDITION);
			}
		}
		
		return super.mutate(n);
	}
	
	private boolean isEmptyOrContainReturnStmt(Statement stmt) {
		if (stmt == null) {
			return true;
		}
		if (stmt instanceof ReturnStmt) {
			return true;
		}
		if (stmt instanceof BlockStmt) {
			List<Statement> subStmts = ((BlockStmt) stmt).getStmts();
			if (CollectionUtils.isEmpty(subStmts)) {
				return true;
			}
			for (Statement subStmt : subStmts) {
				if (subStmt instanceof ReturnStmt) {
					return true;
				}
			}
		}
		return false; 
	}
}
