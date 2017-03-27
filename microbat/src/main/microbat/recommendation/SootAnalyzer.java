package microbat.recommendation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import microbat.model.variable.Variable;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.TempVariableInfo;
import sav.strategies.dto.AppJavaClassPath;
import soot.Body;
import soot.Local;
import soot.MethodSource;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class SootAnalyzer {
	
	class MethodFinder extends ASTVisitor{
		private CompilationUnit cu;
		private int line;
		
		MethodDeclaration md;
		
		public MethodFinder(CompilationUnit cu, int line){
			this.cu = cu;
			this.line = line;
		}
		
		public boolean visit(MethodDeclaration md){
			int startLine = this.cu.getLineNumber(md.getStartPosition());
			int endLine = this.cu.getLineNumber(md.getStartPosition()+md.getLength());
			
			if(startLine<=this.line && this.line<=endLine){
				this.md = md;
				return false;
			}

			return true;
		}
	}
	
	private SootClass getSootClass(String className){
		SootClass c = null;
		for(int i=0; i<10; i++){
			try{
				c = Scene.v().loadClassAndSupport(className);
				if(c!=null){
					break;
				}
			}
			catch(Exception e){}
		}
		
//		SootClass c = Scene.v().loadClassAndSupport(appClassPath.getLaunchClass());
		c.setApplicationClass();
		return c;
	}

	public void analyze(ChosenVariableOption variableOption, CompilationUnit cu, int line) {
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		
		String classPathString = appClassPath.getClasspathStr();
		String rtJar = appClassPath.getJavaHome() + File.separator + "jre" + File.separator + "lib" + File.separator + "rt.jar";
		classPathString += File.pathSeparator + rtJar;
		
		Scene.v().setSootClassPath(classPathString);
		Options.v().set_keep_line_number(true);
		Options.v().set_debug(true);
		Options.v().set_via_shimple(true);
		Options.v().setPhaseOption("jb", "use-original-names");
		
		SootClass c = getSootClass(JavaUtil.getFullNameOfCompilationUnit(cu));
		
		MethodFinder finder = new MethodFinder(cu, line);
		cu.accept(finder);
		MethodDeclaration methodDec = finder.md; 
		
		String methodSignature = constructSootMethodSignature(methodDec);
		
		SootMethod method = c.getMethod(methodSignature);
		
		Body body = method.retrieveActiveBody();
		Chain<Local> chain = body.getLocals();
		for(Local local: chain){
			System.currentTimeMillis();
		}
		
		Variable var = variableOption.getReadVar().getVariable();
		
		UnitGraph graph = new ExceptionalUnitGraph(body);
		List<Unit> units = retrieveUnitsAccordingToLineNumber(graph, line);
		
		List<Unit> seedStatements = findSeeds(var, graph, units);
		System.currentTimeMillis();
		
//		MHGDominatorsFinder<Unit> dominatorFinder = new MHGDominatorsFinder<>(graph);
//		List<Unit> dominators = dominatorFinder.getDominators(unit);
	}

	private List<Unit> findSeeds(Variable var, UnitGraph graph, List<Unit> units) {
		HashSet<Unit> parsedUnits = new HashSet<>();
		List<Unit> allSeeds = new ArrayList<>();
		for(Unit unit: units){
			List<Unit> seeds = new ArrayList<>();
			if(!parsedUnits.contains(unit)){
				findDefinitions(var, unit, seeds, graph, parsedUnits);				
			}
			
			for(Unit seed: seeds){
				if(!allSeeds.contains(seed)){
					allSeeds.add(seed);
				}
			}
		}
		
		return allSeeds;
	}

	private void findDefinitions(Variable var, Unit unit, List<Unit> seeds, UnitGraph graph, HashSet<Unit> parsedUnits) {
		parsedUnits.add(unit);
		for(ValueBox valueBox: unit.getDefBoxes()){
			Value val = valueBox.getValue();
			String variableName = val.toString();
			if(variableName.equals(var.getName())){
				if(!seeds.contains(unit)){
					seeds.add(unit);					
				}
				return;
			}
		}
		
		List<Unit> predessors = graph.getPredsOf(unit);
		for(Unit precessor: predessors){
			if(!parsedUnits.contains(precessor)){
				findDefinitions(var, precessor, seeds, graph, parsedUnits);				
			}
		}
	}

	private List<Unit> retrieveUnitsAccordingToLineNumber(UnitGraph graph, int line) {
		List<Unit> units = new ArrayList<>();
		
		Iterator<Unit> iterator = graph.iterator();
		while(iterator.hasNext()){
			Unit unit = iterator.next();
			List<Tag> tagList = unit.getTags();
			
			Tag tag = tagList.get(0);
			if(tag instanceof LineNumberTag){
				LineNumberTag lTag = (LineNumberTag)tag;
				if(lTag.getLineNumber()==line){
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
		for(Object obj: methodDec.parameters()){
			if(obj instanceof SingleVariableDeclaration){
				SingleVariableDeclaration svd = (SingleVariableDeclaration)obj;
				String typeString = svd.getType().resolveBinding().toString();
				buffer.append(typeString);
				buffer.append(",");
			}
		}
		String signature = buffer.toString();
		if(signature.endsWith(",")){
			signature = signature.substring(0, signature.length()-1);
		}
		signature += ")";
		return signature;
	}

}
