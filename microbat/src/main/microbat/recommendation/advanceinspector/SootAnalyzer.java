package microbat.recommendation.advanceinspector;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SootAnalyzer {
	
	public static boolean firstTimeForPointsToAnalysis = true;

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

	public Map<String, List<Unit>> analyzeSeeds(ChosenVariableOption variableOption, CompilationUnit cu, int line) {
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();

		String classPathString = appClassPath.getClasspathStr();
		String rtJar = appClassPath.getJavaHome() + File.separator + "jre" + File.separator + "lib" + File.separator
				+ "rt.jar";
		classPathString += File.pathSeparator + rtJar;

		if(firstTimeForPointsToAnalysis){
			G.reset();			
		}
		
		Scene.v().setSootClassPath(classPathString);
		Options.v().set_keep_line_number(true);
		Options.v().set_debug(true);
		Options.v().set_via_shimple(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_verbose(true);
		Options.v().set_allow_phantom_refs(true);

		Options.v().setPhaseOption("jb", "use-original-names");

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
//		List<Unit> unitsOfSpecificLineNumber = retrieveUnitsAccordingToLineNumber(graph, line);

//		List<Unit> seedStatements = new SeedGenerator().findSeeds(var, graph, unitsOfSpecificLineNumber);
		Map<String, List<Unit>> seedMap = new SeedGenerator().findSeeds(var, graph, firstTimeForPointsToAnalysis);
		
		Map<String, List<Unit>> seeds = new SeedsFilter().filter(seedMap, var);
		
		firstTimeForPointsToAnalysis = false;

		return seeds;
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

			if(tagList != null && !tagList.isEmpty()){
				Tag tag = tagList.get(0);
				if (tag instanceof LineNumberTag) {
					LineNumberTag lTag = (LineNumberTag) tag;
					if (lTag.getLineNumber() == line) {
						units.add(unit);
					}
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
