package microbat.recommendation.advanceinspector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import microbat.model.BreakPointValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.Variable;
import soot.Local;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

public class SeedGenerator {
	
	public List<Unit> findSeeds(VarValue var, UnitGraph contextGraph){
		List<Unit> seeds = new ArrayList<>();
		
		RelationChain chain = getTopParent(var);
		
		System.currentTimeMillis();
		
		return seeds;
	}
	
	public RelationChain getTopParent(VarValue var){
		RelationChain chain = new RelationChain();
		chain.vars.add(var);
		
		if(var.getParents().isEmpty()){
			return chain;
		}
		
		if(var.getParents().get(0) instanceof BreakPointValue){
			return chain;
		}
		
		VarValue parent = var.getParents().get(0);
		chain.vars.add(parent);
		int relation = parseRelation(var);
		chain.relation.add(relation);
		
		while(parent != null){
			if(parent.getParents().isEmpty()){
				parent = null;
			}
			else if(parent.getParents().get(0) instanceof BreakPointValue){
				parent = null;
			}
			else{
				
				VarValue child = parent;
				for(VarValue par: parent.getParents()){
					if(!chain.vars.contains(par)){
						parent = par;
						break;
					}					
				}
				
				chain.vars.add(parent);
				relation = parseRelation(child);
				chain.relation.add(relation);
			}
		}
		
		return chain;
	}
	
	private int parseRelation(VarValue var) {
		Variable variable = var.getVariable();
		if(variable instanceof ArrayElementVar){
			return RelationChain.ARRAY_ELEMENT;
		}
		else if(variable instanceof FieldVar){
			return RelationChain.FIELD;
		}
		
		System.err.println("The relation is not either array element or field access");
		
		return -1;
	}

	public List<Unit> findSeeds(VarValue var, UnitGraph graph, List<Unit> unitsOfSpecificLineNumber) {
		
		
		
		HashSet<Unit> parsedUnits = new HashSet<>();
		List<Unit> allSeeds = new ArrayList<>();
		for (Unit currentUnit: unitsOfSpecificLineNumber) {
			List<Unit> seeds = new ArrayList<>();
			if (!parsedUnits.contains(currentUnit)) {
				seeds = findVarDefsInGraph(var, currentUnit, seeds, graph, parsedUnits);
			}

			for (Unit seed : seeds) {
				if (!allSeeds.contains(seed)) {
					allSeeds.add(seed);
				}
			}
		}

		return allSeeds;
	}

	/**
	 * reverse traverse the CFG, if a CFG node defines the {@code varValue},
	 * then the algorithm stops propagating. Otherwise, the algorithm traverse
	 * back to the entry CFG node to find the possible CFG node (i.e., unit)
	 * defining the {@code varValue}.
	 * 
	 * @param varValue
	 * @param currentUnit
	 * @param seeds
	 * @param graph
	 * @param parsedUnits
	 */
	private List<Unit> findVarDefsInGraph(VarValue varValue, Unit currentUnit, List<Unit> seeds, UnitGraph graph,
			HashSet<Unit> parsedUnits) {
		parsedUnits.add(currentUnit);

		// List<VarValue> parentChain = retrieveParentChain(varValue);

		checkUnitDefiningInterestingVariable(currentUnit, graph, seeds, varValue);

		List<Unit> predessors = graph.getPredsOf(currentUnit);
		for (Unit precessor : predessors) {
			if (!parsedUnits.contains(precessor)) {
				findVarDefsInGraph(varValue, precessor, seeds, graph, parsedUnits);
			}
		}

		return seeds;
	}

	/**
	 * Check whether the unit defines the {@code varValue}. Note that, if a
	 * variable is an array element or a field of an object instance, e.g.,
	 * a.k[b].c, we need to recursively decide whether the variable
	 * corresponding to this static soot variable in CFG node (unit).
	 * 
	 * @param unit
	 * @param graph
	 * @param seeds
	 * @param varValue
	 * @return
	 */
	private boolean checkUnitDefiningInterestingVariable(Unit unit, UnitGraph graph, List<Unit> seeds,
			VarValue varValue) {

		for (ValueBox valueBox : unit.getDefBoxes()) {
			Value val = valueBox.getValue();

			if (isTypeMatch(val, varValue)) {
				
				if (!varValue.getParents().isEmpty() && !(varValue.getParents().get(0) instanceof BreakPointValue)) {
					boolean isRecursiveMatch = resursiveMatch(val, unit, varValue, graph);
					if (isRecursiveMatch) {
						if (!seeds.contains(unit)) {
							seeds.add(unit);
						}
						return true;
					}
				} else {
					boolean isMatch = match(val, varValue);
					if (isMatch) {
						if (!seeds.contains(unit)) {
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

		if (type.toString().equals(varValue.getVariable().getType())) {
			return true;
		}

		return false;
	}

	/**
	 * This method is written with regard to the assumption that Soot performs
	 * the ``linearization'' which decompose an expression into a series of
	 * three-address instructions. It means if there is a very complicated
	 * expression like ``a.k[b].c'', Soot must decompose it into a series of
	 * simple variable definitions.
	 * 
	 * Therefore, we start from {@code definingUnit} which defines the
	 * interested variable {@code varValue}, and go back in CFG to search the
	 * instructions of linearization. Thus, we can recursively match the parents
	 * of interested variable to these linearized instruction to see whether two
	 * chains of variables are matched. For example, obj.array[1] shall be
	 * matched to "$r0[1]; $r0 = obj.<sort.quick.Obj: int[] array>; obj = $r2;".
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
		while (parent != null) {
			Value parentVal = findParent(val, definingUnit, graph);
			if (parentVal == null || !match(parentVal, parent)) {
				return false;
			} else {
				if (parent.getParents().isEmpty()) {
					parent = null;
				} else {
					parent = parent.getParents().get(0);
					if (parent instanceof BreakPointValue) {
						parent = null;
					}
					val = parentVal;
				}
			}
		}

		return true;
	}
	
	private boolean match(Value val, VarValue varValue) {
		Variable var = varValue.getVariable();
		if ((val instanceof JimpleLocal)) {
			JimpleLocal local = (JimpleLocal) val;
			if (local.getName().equals(var.getName())) {
				return true;
			}
		}

		if ((val instanceof FieldRef) && (var instanceof FieldVar)) {
			FieldRef fieldRef = (FieldRef) val;
			if (fieldRef.getField().getName().equals(var.getName())) {
				return true;
			}
		}

		if (val instanceof ArrayRef) {
			ArrayRef arrayRef = (ArrayRef) val;
			// TODO
			System.currentTimeMillis();
		}
		return false;
	}
	
	private Value findParent(Value val, Unit unit, UnitGraph graph) {
		Unit predessor = null;

		List<Unit> predessors = graph.getPredsOf(unit);
		if (!predessors.isEmpty()) {
			predessor = predessors.get(0);
		}

		while (predessor != null) {
			if (val instanceof ArrayRef) {
				ArrayRef arrayRef = (ArrayRef) val;
				Value parent = arrayRef.getBase();
				if (isVarDefinedInUnit(parent, predessor)) {
					if (predessor instanceof AssignStmt) {
						AssignStmt assign = (AssignStmt) predessor;
						Value rightOp = assign.getRightOp();
						return rightOp;
					}
				}

			} else if (val instanceof InstanceFieldRef) {
				InstanceFieldRef insFieldRef = (InstanceFieldRef) val;
				Value parent = insFieldRef.getBase();
				if (isVarDefinedInUnit(parent, predessor)) {
					return parent;
				}
			} else if (val instanceof Local) {
				Local local = (Local) val;
				// TODO
				System.currentTimeMillis();
			}

			if (!graph.getPredsOf(predessor).isEmpty()) {
				predessor = graph.getPredsOf(predessor).get(0);
			} else {
				predessor = null;
			}
		}

		return null;
	}
	
	private boolean isVarDefinedInUnit(Value var, Unit unit) {
		for (ValueBox box : unit.getDefBoxes()) {
			if (box.getValue().equals(var)) {
				return true;
			}
		}
		return false;
	}
}
