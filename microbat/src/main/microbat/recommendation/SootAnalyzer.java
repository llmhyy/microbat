package microbat.recommendation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import microbat.model.BreakPointValue;
import microbat.model.value.VarValue;
import microbat.model.variable.FieldVar;
import microbat.model.variable.Variable;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;
import soot.Body;
import soot.Local;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.geom.geomPA.GeomPointsTo;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

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
		SootClass c = Scene.v().loadClassAndSupport(className);
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
//		Options.v().set_keep_offset(true);
		Options.v().set_debug(true);
		Options.v().set_via_shimple(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_verbose(true);
		Options.v().set_allow_phantom_refs(true);
		
		Options.v().setPhaseOption("jb", "use-original-names:true");
		
		Options.v().setPhaseOption("cg","verbose");
		Options.v().setPhaseOption("cg.spark", "enabled:true");
		Options.v().setPhaseOption("cg.spark", "geom-pta:true");
		Options.v().setPhaseOption("cg.spark", "simplify-offline:false");
		Options.v().setPhaseOption("cg.spark", "geom-runs:5");
		
		SootClass c = getSootClass(JavaUtil.getFullNameOfCompilationUnit(cu));
		Scene.v().loadNecessaryClasses();
		
		HashMap<String, String> opt = new HashMap<>();
		opt.put("verbose","true");
		opt.put("propagator","worklist");
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");
		opt.put("double-set-new","hybrid");
		SparkTransformer.v().transform("", opt);
		
		MethodFinder finder = new MethodFinder(cu, line);
		cu.accept(finder);
		MethodDeclaration methodDec = finder.md; 
		
		String methodSignature = constructSootMethodSignature(methodDec);
		
		SootMethod method = c.getMethod(methodSignature);
		
		Body body = method.retrieveActiveBody();
		VarValue var = variableOption.getReadVar();
		
		UnitGraph graph = new ExceptionalUnitGraph(body);
		List<Unit> units = retrieveUnitsAccordingToLineNumber(graph, line);
		
		List<Unit> seedStatements = findSeeds(var, graph, units);
		
		for(Unit seedStatement: seedStatements){
			ValueBox box = seedStatement.getDefBoxes().get(0);
			Value val = box.getValue();
			if(val instanceof Local){
				PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
				GeomPointsTo geomPTA = (GeomPointsTo)pta;
				PointsToSetInternal pts = (PointsToSetInternal)geomPTA.reachingObjects((Local)val);

				pts.forall( new P2SetVisitor() {

				    public void visit(Node n) {

				        // Do what you like with n, which is in the type of AllocNode
				    	if(n instanceof AllocNode){
				    		AllocNode allocNode = (AllocNode)n;
				    		SootMethod method = allocNode.getMethod();
				    		
				    		
				    		for(Local local: method.getActiveBody().getLocals()){
				    			Object obj = allocNode.getNewExpr();
				    			if(obj instanceof JNewArrayExpr){
				    				JNewArrayExpr expr = (JNewArrayExpr)obj;
				    				System.currentTimeMillis();
				    			}
				    			if(local.getNumber()==2){
				    				System.currentTimeMillis();
				    			}
				    		}
				    	}
				    	System.currentTimeMillis();
				    	System.currentTimeMillis();
				    }

				});
			}
			
			
		}
		
		
		
//		MHGDominatorsFinder<Unit> dominatorFinder = new MHGDominatorsFinder<>(graph);
//		List<Unit> dominators = dominatorFinder.getDominators(unit);
	}

	private List<Unit> findSeeds(VarValue var, UnitGraph graph, List<Unit> units) {
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

	/**
	 * reverse traverse the CFG, if a CFG node defines the {@code varValue}, then the algorithm
	 * stops propagating. Otherwise, the algorithm traverse back to the entry CFG node to find the
	 * possible CFG node (i.e., unit) defining the {@code varValue}.
	 * 
	 * @param varValue
	 * @param unit
	 * @param seeds
	 * @param graph
	 * @param parsedUnits
	 */
	private void findDefinitions(VarValue varValue, Unit unit, List<Unit> seeds, 
			UnitGraph graph, HashSet<Unit> parsedUnits) {
		parsedUnits.add(unit);

//		List<VarValue> parentChain = retrieveParentChain(varValue);
		
		checkUnitDefiningInterestingVariable(unit, graph, seeds, varValue);
		
		List<Unit> predessors = graph.getPredsOf(unit);
		for(Unit precessor: predessors){
			if(!parsedUnits.contains(precessor)){
				findDefinitions(varValue, precessor, seeds, graph, parsedUnits);				
			}
		}
		
	}

	/**
	 * Check whether the unit defines the {@code varValue}. Note that, if a variable is an array
	 * element or a field of an object instance, e.g., a.k[b].c, we need to recursively decide whether
	 * the variable corresponding to this static soot variable in CFG node (unit).
	 * 
	 * @param unit
	 * @param graph
	 * @param seeds
	 * @param varValue
	 * @return
	 */
	private boolean checkUnitDefiningInterestingVariable(Unit unit, UnitGraph graph, List<Unit> seeds, 
			VarValue varValue) {
		
		for(ValueBox valueBox: unit.getDefBoxes()){
			Value val = valueBox.getValue();
			
			if(isTypeMatch(val, varValue)){
				/**
				 * every varValue has a parent node of type BreakPointValue
				 */
				if(!varValue.getParents().isEmpty() && !(varValue.getParents().get(0) instanceof BreakPointValue)){
					boolean isRecursiveMatch = resursiveMatch(val, unit, varValue, graph);
					if(isRecursiveMatch){
						if(!seeds.contains(unit)){
							seeds.add(unit);					
						}
						return true;
					}
				}
				else{
					boolean isMatch = match(val, varValue);
					if(isMatch){
						if(!seeds.contains(unit)){
							seeds.add(unit);					
						}
						return true;
					}
				}
			}
			
		}
		
		return false;
	}

	private boolean isTypeMatch(Value val, VarValue varValue) {
		Type type = val.getType();
		
		if(type.toString().equals(varValue.getVariable().getType())){
			return true;
		}
		
		return false;
	}
	
	/**
	 * This method is written with regard to the assumption that Soot performs the ``linearization''
	 * which decompose an expression into a series of three-address instructions. It means if there 
	 * is a very complicated expression like ``a.k[b].c'', Soot must decompose it into a series of
	 * simple variable definitions.
	 * 
	 * Therefore, we start from {@code definingUnit} which defines the interested variable {@code varValue}, 
	 * and go back in CFG to search the instructions of linearization. Thus, we can recursively match
	 * the parents of interested variable to these linearized instruction to see whether two chains of
	 * variables are matched. For example, obj.array[1] shall be matched to 
	 * "$r0[1]; $r0 = obj.<sort.quick.Obj: int[] array>; obj = $r2;".
	 *  
	 * @param val
	 * @param definingUnit
	 * @param varValue
	 * @param graph
	 * @return
	 */
	private boolean resursiveMatch(Value val, Unit definingUnit, VarValue varValue, UnitGraph graph) {
		VarValue parent = varValue.getParents().get(0);
		System.currentTimeMillis();
		while(parent != null){
			Value parentVal = findParent(val, definingUnit, graph);
			if(parentVal==null || !match(parentVal, parent)){
				return false;
			}
			else{
				if(parent.getParents().isEmpty()){
					parent = null;
				}
				else{
					parent = parent.getParents().get(0);	
					if(parent instanceof BreakPointValue){
						parent = null;
					}
					val = parentVal;
				}
			}
		}
		
		return true;
	}

	private Value findParent(Value val, Unit unit, UnitGraph graph) {
		Unit predessor = null;
		
		List<Unit> predessors = graph.getPredsOf(unit);
		if(!predessors.isEmpty()){
			predessor = predessors.get(0);			
		}
		
		while(predessor!=null){
			if(val instanceof ArrayRef){
				ArrayRef arrayRef = (ArrayRef)val;
				Value parent = arrayRef.getBase();
				if(isDefined(parent, predessor)){
					if(predessor instanceof AssignStmt){
						AssignStmt assign = (AssignStmt)predessor;
						Value rightOp = assign.getRightOp();
						return rightOp;
						
//						if(rightOp instanceof FieldRef){
//							return rightOp;
//						}
					}
				}
				
			}
			else if(val instanceof InstanceFieldRef){
				InstanceFieldRef insFieldRef = (InstanceFieldRef)val;
				Value parent = insFieldRef.getBase();
				if(isDefined(parent, predessor)){
					return parent;
				}
			}
			else if(val instanceof Local){
				Local local = (Local)val;
				//TODO
				System.currentTimeMillis();
			}
			
			if(!graph.getPredsOf(predessor).isEmpty()){
				predessor = graph.getPredsOf(predessor).get(0);			
			}
			else{
				predessor = null;
			}
		}
		
		return null;
	}

	private boolean isDefined(Value parent, Unit unit) {
		for(ValueBox box: unit.getDefBoxes()){
			if(box.getValue().equals(parent)){
				return true;
			}
		}
		return false;
	}

	private Unit findNearestDefinition(Value parent, Unit predessor) {
		
		return null;
	}

	private boolean match(Value val, VarValue varValue) {
		Variable var = varValue.getVariable();
		if((val instanceof JimpleLocal)){
			JimpleLocal local = (JimpleLocal)val;
			if(local.getName().equals(var.getName())){
				return true;
			}
		}
		
		if((val instanceof FieldRef) && (var instanceof FieldVar)){
			FieldRef fieldRef = (FieldRef)val;
			if(fieldRef.getField().getName().equals(var.getName())){
				return true;
			}
		}
		
		if(val instanceof ArrayRef){
			ArrayRef arrayRef = (ArrayRef)val;
			//TODO
			System.currentTimeMillis();
		}
		return false;
	}

	private Local findDefinedVar(Local parent, Unit unitDefiningParent, VarValue child, List<VarValue> parentChain, int i) {
		if(i<0){
			
		}
		
		
		if(child.isElementOfArray()){
//			Unit accessingUnit = findUnitAccessingArrayElementOfParent();
//			ValueBox valBox = accessingUnit.getDefBoxes().get(0);
//			Local local = (Local)valBox.getValue();
			
		}
		else if(child.isField()){
			
		}
		
		return null;
	}

	private List<VarValue> retrieveParentChain(VarValue varValue) {
		List<VarValue> parentChain = new ArrayList<>();
		VarValue parent = null;
		if(!varValue.getParents().isEmpty()){
			parent = varValue.getParents().get(0);
		}
		
		while(parent!=null){
			if(!parentChain.contains(parent) && !(parent instanceof BreakPointValue)){
				parentChain.add(parent);
				if(!parent.getParents().isEmpty()){
					parent = parent.getParents().get(0);
				}				
			}
			else{
				break;
			}
		}
		
		return parentChain;
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
