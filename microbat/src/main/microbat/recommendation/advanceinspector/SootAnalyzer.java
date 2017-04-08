package microbat.recommendation.advanceinspector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;
import soot.Body;
import soot.G;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SootAnalyzer {

	class MethodFinder extends ASTVisitor {
		private CompilationUnit cu;
		private int line;

		MethodDeclaration md;

		public MethodFinder(CompilationUnit cu, int line) {
			this.cu = cu;
			this.line = line;
		}

		public boolean visit(MethodDeclaration md) {
			int startLine = this.cu.getLineNumber(md.getStartPosition());
			int endLine = this.cu.getLineNumber(md.getStartPosition() + md.getLength());

			if (startLine <= this.line && this.line <= endLine) {
				this.md = md;
				return false;
			}

			return true;
		}
	}

	public void analyze(ChosenVariableOption variableOption, CompilationUnit cu, int line) {
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();

		String classPathString = appClassPath.getClasspathStr();
		String rtJar = appClassPath.getJavaHome() + File.separator + "jre" + File.separator + "lib" + File.separator
				+ "rt.jar";
		classPathString += File.pathSeparator + rtJar;

//		G.reset();
		Scene.v().setSootClassPath(classPathString);
		Options.v().set_keep_line_number(true);
		Options.v().set_debug(true);
		Options.v().set_via_shimple(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_verbose(true);
		Options.v().set_allow_phantom_refs(true);

		Options.v().setPhaseOption("jb", "use-original-names:true");

		SootClass c = Scene.v().loadClassAndSupport(JavaUtil.getFullNameOfCompilationUnit(cu));
		c.setApplicationClass();
		Scene.v().loadNecessaryClasses();

		MethodFinder finder = new MethodFinder(cu, line);
		cu.accept(finder);
		MethodDeclaration methodDec = finder.md;

		String methodSignature = constructSootMethodSignature(methodDec);

		SootMethod method = c.getMethod(methodSignature);

		Body body = method.retrieveActiveBody();
		VarValue var = variableOption.getReadVar();

		UnitGraph graph = new ExceptionalUnitGraph(body);
		List<Unit> unitsOfSpecificLineNumber = retrieveUnitsAccordingToLineNumber(graph, line);

//		List<Unit> seedStatements = new SeedGenerator().findSeeds(var, graph, unitsOfSpecificLineNumber);
		List<Unit> seedStatements = new SeedGenerator().findSeeds(var, graph);
		
		System.currentTimeMillis();

//		for (Unit seedStatement : seedStatements) {
//			ValueBox box = seedStatement.getDefBoxes().get(0);
//			Value val = box.getValue();
//			if (val instanceof Local) {
//				PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
//				// GeomPointsTo geomPTA = (GeomPointsTo)pta;
//				// PointsToSetInternal pts =
//				// (PointsToSetInternal)geomPTA.reachingObjects((Local)val);
//
//				SootMethod method0 = c.getMethodByName("sort");
//				Local l = method0.getActiveBody().getLocals().getFirst();
////				l = method0.getActiveBody().getLocals().getSuccOf(l);
//				
//				SootField field = c.getFieldByName("arr");
//				
//				PAG geomPTA = (PAG) pta;
//				
//				PointsToSet set = geomPTA.reachingObjects((Local) val);
//				PointsToSet set2 = geomPTA.reachingObjects(l);
//				PointsToSet set3 = geomPTA.reachingObjects(set2, field);
//				
//				DoublePointsToSet pts = (DoublePointsToSet) geomPTA.reachingObjects((Local) val);
//				System.out.println(pts);
//
//				pts.forall(new P2SetVisitor() {
//
//					public void visit(Node n) {
//
//						// Do what you like with n, which is in the type of
//						// AllocNode
//						if (n instanceof AllocNode) {
//							AllocNode allocNode = (AllocNode) n;
//							SootMethod method = allocNode.getMethod();
//
//							for (Local local : method.getActiveBody().getLocals()) {
//								Object obj = allocNode.getNewExpr();
//								if (obj instanceof JNewArrayExpr) {
//									JNewArrayExpr expr = (JNewArrayExpr) obj;
//									System.currentTimeMillis();
//								}
//								if (local.getNumber() == 2) {
//									System.currentTimeMillis();
//								}
//							}
//						}
//						System.currentTimeMillis();
//						System.currentTimeMillis();
//					}
//
//				});
//			}
//
//		}

		// MHGDominatorsFinder<Unit> dominatorFinder = new
		// MHGDominatorsFinder<>(graph);
		// List<Unit> dominators = dominatorFinder.getDominators(unit);
	}

	private List<Unit> retrieveUnitsAccordingToLineNumber(UnitGraph graph, int line) {
		List<Unit> units = new ArrayList<>();

		Iterator<Unit> iterator = graph.iterator();
		while (iterator.hasNext()) {
			Unit unit = iterator.next();
			List<Tag> tagList = unit.getTags();

			Tag tag = tagList.get(0);
			if (tag instanceof LineNumberTag) {
				LineNumberTag lTag = (LineNumberTag) tag;
				if (lTag.getLineNumber() == line) {
					units.add(unit);
				}
			}
		}

		return units;
	}

	private String constructSootMethodSignature(MethodDeclaration methodDec) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(methodDec.getReturnType2());
		buffer.append(" ");
		buffer.append(methodDec.getName().getIdentifier());
		buffer.append("(");
		for (Object obj : methodDec.parameters()) {
			if (obj instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
				String typeString = svd.getType().resolveBinding().toString();
				buffer.append(typeString);
				buffer.append(",");
			}
		}
		String signature = buffer.toString();
		if (signature.endsWith(",")) {
			signature = signature.substring(0, signature.length() - 1);
		}
		signature += ")";
		return signature;
	}

}
