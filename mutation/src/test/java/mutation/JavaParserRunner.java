/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.ExpressionStmt;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.CollectionUtils;
import sav.commons.TestConfiguration;
import sav.commons.testdata.calculator.Sum;

/**
 * @author LLT
 *
 */
public class JavaParserRunner {

	@Test
	public void run2() throws Exception {
		String orgStr = "			/*abc£*/return result;";
		String str = StringEscapeUtils.escapeJava(orgStr);
		System.out.println(str.length());
		System.out.println(str);
		
	}
	
	@Test
	public void parseStmt() throws Exception {
		MethodCallExpr methodCallExpr = new MethodCallExpr();
		methodCallExpr.setNameExpr(new NameExpr("println"));
		methodCallExpr.setScope(new FieldAccessExpr(new NameExpr("System"),
				"out"));
		List<Expression> args = CollectionUtils
				.listOf((Expression) new StringLiteralExpr("abc"));
		methodCallExpr.setArgs(args);
		ExpressionStmt stmt = new ExpressionStmt(methodCallExpr);
		System.out.println(stmt);
//		Statement stmt = JavaParser.parseStatement("System.out.println(\"abc\");");
//		System.out.println(stmt);
	}
	
	@Test
	public void runThis() throws Exception {
		String jfile = "./src/test/java/mutation/JavaParserRunner.java";
		CompilationUnit cu = JavaParser.parse(new File(
				jfile));
		recursive(cu);
		assert true;
	}
	
	@Test
	public void run1() throws Exception {
		CompilationUnit cu = JavaParser.parse(new File(
				"./src/test/java/test/parser/SecondClassCaller.java"));
		System.out.println(cu);
	}
	
	@Test
	public void run() throws Exception {
		String jfile = "./src/test/java/testdata/filewriter/FileWriterTestData.java";
		CompilationUnit cu = JavaParser.parse(new File(
				jfile));
		
		recursive(cu);
		assert true;
	}
	
	@Test
	public void runSum() throws Exception {
		CompilationUnit cu = JavaParser.parse(new File(
				ClassUtils.getJFilePath(TestConfiguration.getTestScrPath("sav.commons"), 
						Sum.class.getName())));
		recursive(cu);
	}
	
	/**
	 * \n recursive function
	 * */
	private void recursive(Node node) {
		for (Node child : node.getChildrenNodes()) {
			System.out.println("---------------------------------------"); System.out.println(String.
					format("%s from %s, %s",
					child.getClass().getName(),
					child.getBeginLine(), child.getBeginColumn()));
			System.out.println(child);
			recursive(child);
		}
	}
	
	public static class TestClass {
		private int result;
		
		public static void initTestClass() {
			TestClass clazz = new TestClass();
			int i = 100;
			int x = clazz.getResult();
			assert "a" != null;
			System.out.println(i); System.out.println(x);
			if (i > 10) {
				System.out.println("i > 10");
			}
			;
		}
		
		//abc\u00A3  abc£
		public int getResult() {

			/*abc\u00A3\\*/return result;
		}
	}
}
