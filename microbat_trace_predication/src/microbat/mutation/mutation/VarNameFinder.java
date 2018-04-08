package microbat.mutation.mutation;

import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class VarNameFinder extends VoidVisitorAdapter<Boolean> {
	private String varName;
	
	public void reset() {
		this.varName = null;
	}
	
	@Override
	public void visit(FieldAccessExpr n, Boolean arg) {
		visit(n.getFieldExpr(), arg);
	}
	
	@Override
	public void visit(NameExpr n, Boolean arg) {
		varName = n.getName();
	}
	
	public String getVarName() {
		return varName;
	}
}
