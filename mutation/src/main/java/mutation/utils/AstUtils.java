/**
 * 
 */
package mutation.utils;

import java.util.List;

import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.ThrowStmt;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class AstUtils {
	private AstUtils(){}
	
	public static boolean isReturnStmt(Statement stmt) {
		if (stmt == null) {
			return false;
		}
		if (isReturnOrThrowStmt(stmt)) {
			return true;
		}
		if (stmt instanceof BlockStmt) {
			List<Statement> subStmts = ((BlockStmt) stmt).getStmts();
			if (CollectionUtils.isEmpty(subStmts)) {
				return false;
			}
			if (subStmts.size() > 1) {
				return false;
			}
			if (isReturnOrThrowStmt(subStmts.get(0))) {
				return true;
			}
		}
		return false; 
	}
	
	public static boolean isReturnOrThrowStmt(Statement stmt) {
		return (stmt instanceof ReturnStmt) || (stmt instanceof ThrowStmt);
	}

	public static boolean isEmpty(Statement stmt) {
		if (stmt == null) {
			return true;
		}
		if (stmt instanceof BlockStmt) {
			List<Statement> subStmts = ((BlockStmt) stmt).getStmts();
			if (CollectionUtils.isEmpty(subStmts)) {
				return true;
			}
		}
		return false; 
	}
	
	public static boolean doesContainReturnStmt(Statement stmt) {
		if (stmt == null) {
			return false;
		}
		if (isReturnOrThrowStmt(stmt)) {
			return true;
		}
		if (stmt instanceof BlockStmt) {
			List<Statement> subStmts = ((BlockStmt) stmt).getStmts();
			for (Statement subStmt : subStmts) {
				if (isReturnOrThrowStmt(subStmt)) {
					return true;
				}
			}
		}
		return false; 
	}

}
