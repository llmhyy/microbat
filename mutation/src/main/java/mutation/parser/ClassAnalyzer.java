/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.parser;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 * 
 */
public class ClassAnalyzer {
	private String sourceFolder;
	private ClassManager cm;
	public IJavaParser javaParser;

	public ClassAnalyzer(String srcFolder, IJavaParser javaParser) {
		this.sourceFolder = srcFolder;
		cm = new ClassManager(sourceFolder);
		this.javaParser = javaParser;
	}

	public ClassManager analyzeFolder(File folder) {
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				List<ClassDescriptor> cds = analyzeJavaFile(file);
				for (ClassDescriptor cd : cds) {
					cd.register(cm);
				}
			} else {
				analyzeFolder(file);
			}
		}

		return cm;
	}

	public List<ClassDescriptor> analyzeJavaFile(File javaFile) {
		try {
			CompilationUnit cu = javaParser.parse(javaFile);
			return analyzeCompilationUnit(cu);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<ClassDescriptor>();
	}

	public List<ClassDescriptor> analyzeCompilationUnit(CompilationUnit cu) {
		List<TypeDeclaration> types = cu.getTypes();
		List<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

		for (TypeDeclaration type : types) {
			if (type instanceof ClassOrInterfaceDeclaration) {
				ClassDescriptor cl = parseClassOrInterface(cu,
						(ClassOrInterfaceDeclaration) type);
				classDescriptors.add(cl);
			}
		}

		return classDescriptors;
	}

	public ClassDescriptor parseClassOrInterface(CompilationUnit cu,
			ClassOrInterfaceDeclaration clDecl) {
		ClassDescriptor cl = new ClassDescriptor();
		cl.setName(clDecl.getName());
		PackageDeclaration pd = cu.getPackage();
		if (pd != null) {
			cl.setPackageName(pd.getName().toString());
		}
		cl.setModifier(clDecl.getModifiers());

		List<ClassOrInterfaceType> extendList = clDecl.getExtends();
		if (extendList != null && extendList.size() > 0) {
			cl.setSuperClass(extendList.get(0).getName());
		}

		List<ClassOrInterfaceType> implList = clDecl.getImplements();
		if (implList != null) {
			for (ClassOrInterfaceType impl : implList) {
				cl.addImplementedInterfaces(impl.getName());
			}
		}

		List<BodyDeclaration> members = clDecl.getMembers();
		for (BodyDeclaration mem : members) {
			if (mem instanceof MethodDeclaration) {
				MethodDescriptor md = parseMethod((MethodDeclaration) mem);
				cl.addMethod(md);
			} else if (mem instanceof FieldDeclaration) {
				List<VariableDescriptor> vars = parseFieldDecl((FieldDeclaration) mem);
				cl.addFields(vars);
			} else if (mem instanceof ClassOrInterfaceDeclaration) {
				ClassDescriptor inner = parseClassOrInterface(cu,
						(ClassOrInterfaceDeclaration) mem);
				inner.setOutterClass(cl);
				cl.addInnerClass(inner);
			}
		}

		return cl;
	}

	public MethodDescriptor parseMethod(MethodDeclaration medDecl) {
		MethodDescriptor med = new MethodDescriptor(medDecl.getBeginLine(),
				medDecl.getEndLine());

		med.setName(medDecl.getName());
		med.setModifier(medDecl.getModifiers());
		med.setReturnType(medDecl.getType());
		List<Parameter> params = medDecl.getParameters();

		med.openScope(medDecl.getBeginLine());
		if (params != null) {
			for (Parameter p : params) {
				VariableDescriptor vp = parseParameter(p);
				med.addParamter(vp);
				med.addLocalVar(vp);
			}
		}
		// get local variables in the method body
		parseBlockStmt(med, medDecl.getBody());

		med.closeScope(medDecl.getEndLine());

		return med;
	}

	private void parseBlockStmt(MethodDescriptor md, Statement statement) {
		// md.openScope(statement.getBeginLine());
		//
		// List<Statement> body;
		// if (statement instanceof BlockStmt)
		// {
		// body = ((BlockStmt) statement).getStmts();
		// }
		// else
		// {
		// body = new ArrayList<>();
		// body.add(statement);
		// }
		//
		// if (body != null)
		// {
		// for (Statement stmt : body)
		// {
		// if (stmt instanceof ExpressionStmt)
		// {
		// Expression exp =((ExpressionStmt) stmt).getExpression();
		// if (exp instanceof VariableDeclarationExpr)
		// {
		//
		// }
		// System.out.println(stmt);
		// System.out.println(stmt.getClass());
		// }
		// else if (stmt instanceof BlockStmt)
		// {
		// parseBlockStmt(md, (BlockStmt)stmt);
		// }
		// else if (stmt instanceof IfStmt)
		// {
		// Statement thenStmt = ((IfStmt) stmt).getThenStmt();
		// parseBlockStmt(md, thenStmt);
		// Statement elseStmt = ((IfStmt) stmt).getElseStmt();
		// parseBlockStmt(md, elseStmt);
		// }
		// else if (stmt instanceof WhileStmt)
		// {
		// Statement whileBody = ((WhileStmt)stmt).getBody();
		// parseBlockStmt(md, whileBody);
		// }
		// else if (stmt instanceof ForeachStmt)
		// {
		// Statement forBody = ((ForeachStmt) stmt).getBody();
		// parseBlockStmt(md, forBody);
		// }
		// else if (stmt instanceof ForStmt)
		// {
		// Statement forBody = ((ForStmt) stmt).getBody();
		// parseBlockStmt(md, forBody);
		// }
		// else if (stmt instanceof SwitchStmt)
		// {
		// List<SwitchEntryStmt> swBody = ((SwitchStmt) stmt).getEntries();
		// for (SwitchEntryStmt swEntry : swBody)
		// {
		// swEntry.getStmts(); fdsafweoruaofi shit here
		// }
		// }
		// }
		// }
		//
		// md.closeScope(statement.getEndLine());

		if (statement instanceof BlockStmt) {
			md.openScope(statement.getBeginLine());

			List<Statement> bodyStmt = ((BlockStmt) statement).getStmts();
			if (bodyStmt != null) {
				for (Statement innerStmt : bodyStmt) {
					parseBlockStmt(md, innerStmt);
				}
			}

			md.closeScope(statement.getEndLine());
		} else if (statement instanceof IfStmt) {
			Statement thenStmt = ((IfStmt) statement).getThenStmt();
			parseBlockStmt(md, thenStmt);
			Statement elseStmt = ((IfStmt) statement).getElseStmt();
			parseBlockStmt(md, elseStmt);
		} else if (statement instanceof WhileStmt) {
			Statement whileBody = ((WhileStmt) statement).getBody();
			parseBlockStmt(md, whileBody);
		} else if (statement instanceof ForStmt) {
			List<Expression> forInit = ((ForStmt) statement).getInit();
			md.openScope(statement.getBeginLine());
			for (Expression init : CollectionUtils.initIfEmpty(forInit)) {
				if (init instanceof VariableDeclarationExpr) {
					List<VariableDescriptor> vList = parseVarDecl((VariableDeclarationExpr) init);
					md.addLocalVars(vList);
				}
			}

			Statement forBody = ((ForStmt) statement).getBody();
			// if there is only one statement in the body of for loop then it
			// cannot be a variable declaration statement
			if (forBody instanceof BlockStmt) {
				List<Statement> bodyStmt = ((BlockStmt) forBody).getStmts();
				for (Statement innerStmt : CollectionUtils.initIfEmpty(bodyStmt)) {
					parseBlockStmt(md, innerStmt);
				}
			}

			// parseBlockStmt(md, forBody);
			md.closeScope(statement.getEndLine());
		} else if (statement instanceof ForeachStmt) {
			ForeachStmt foreachStmt = (ForeachStmt) statement;
			/* add declared variable*/
			md.addLocalVars(parseVarDecl(foreachStmt.getVariable()));
			Statement forBody = foreachStmt.getBody();
			parseBlockStmt(md, forBody);
		} else if (statement instanceof SwitchStmt) {
			List<SwitchEntryStmt> entries = ((SwitchStmt) statement)
					.getEntries();

			for (SwitchEntryStmt e : entries) {
				List<Statement> eBody = e.getStmts();
				for (Statement eStmt : CollectionUtils.initIfEmpty(eBody)) {
					parseBlockStmt(md, eStmt);
				}
			}
		} else if (statement instanceof ExpressionStmt) // add the variable to
														// the currently open
														// scope
		{
			Expression exp = ((ExpressionStmt) statement).getExpression();
			if (exp instanceof VariableDeclarationExpr) {
				List<VariableDescriptor> vList = parseVarDecl((VariableDeclarationExpr) exp);
				md.addLocalVars(vList);
			}
		}
	}

	public List<VariableDescriptor> parseVarDecl(VariableDeclarationExpr varDecl) {
		List<VariableDescriptor> vList = new ArrayList<VariableDescriptor>();

		Type type = ((VariableDeclarationExpr) varDecl).getType();
		List<VariableDeclarator> vars = ((VariableDeclarationExpr) varDecl)
				.getVars();
		for (VariableDeclarator var : vars) {
			VariableDescriptor vd = new VariableDescriptor();
			VariableDeclaratorId vid = var.getId();
			vd.setName(vid.getName());
			vd.setModifier(varDecl.getModifiers());
			vd.setDimension(vid.getArrayCount());
			vd.setType(type);
			vd.setPosition(Position.fromNode(vid));

			
			vList.add(vd);
		}

		return vList;
	}

	public VariableDescriptor parseParameter(Parameter p) {
		VariableDescriptor vp = new VariableDescriptor();
		vp.setModifier(p.getModifiers());
		vp.setType(p.getType());
		vp.setDimension(p.getId().getArrayCount());
		vp.setName(p.getId().getName());
		vp.setPosition(Position.fromNode(p));


		return vp;
	}

	public List<VariableDescriptor> parseFieldDecl(FieldDeclaration fieldDecl) {
		List<VariableDescriptor> vds = new ArrayList<VariableDescriptor>();
		List<VariableDeclarator> vids = fieldDecl.getVariables();

		for (VariableDeclarator vid : vids) {
			VariableDescriptor vd = parseFieldDeclId(fieldDecl, vid.getId());
			vds.add(vd);
		}

		return vds;
	}

	public VariableDescriptor parseFieldDeclId(FieldDeclaration fieldDecl,
			VariableDeclaratorId vId) {
		VariableDescriptor vd = new VariableDescriptor();
		vd.setName(vId.getName());
		vd.setType(fieldDecl.getType());
		vd.setDimension(vId.getArrayCount());
		vd.setPosition(Position.fromNode(vId));
		if (vd.getType() instanceof ReferenceType) {
			// System.out.println(vd.type);
			vd.setDimension(vd.getDimension()
					+ ((ReferenceType) vd.getType()).getArrayCount());
		}
		vd.setModifier(fieldDecl.getModifiers());

		return vd;
	}

}
