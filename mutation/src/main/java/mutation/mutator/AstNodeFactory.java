/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.mutator;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.type.Type;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class AstNodeFactory {
	
	public static NameExpr nameExpr(String name) {
		return new NameExpr(name);
	}
	
	public static StringLiteralExpr expression(String value) {
		return new StringLiteralExpr(value);
	}
	
	public static AssertStmt assertNotNullStmt(Expression expr) {
		BinaryExpr binaryExpr = new BinaryExpr(expr, new NullLiteralExpr(),
				BinaryExpr.Operator.notEquals);
		AssertStmt newStmt = new AssertStmt(binaryExpr);
		return newStmt;
	}
	
	public static ExpressionStmt declarationStmt(Type type, String varName, Expression value) {
		VariableDeclarator varDec = new VariableDeclarator(
				new VariableDeclaratorId(varName));
		VariableDeclarationExpr varDecExpr = new VariableDeclarationExpr(type,
				CollectionUtils.listOf(varDec));
		return assignStmt(varDecExpr, value);
	}
	
	public static ExpressionStmt assignStmt(Expression target, Expression value) {
		AssignExpr expr = new AssignExpr();
		expr.setTarget(target);
		expr.setValue(value);
		expr.setOperator(AssignExpr.Operator.assign);
		return new ExpressionStmt(expr);
	}
	
	public static ReturnStmt returnStmt(Expression returnExpr) {
		return new ReturnStmt(returnExpr);
	}
}
