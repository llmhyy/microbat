package microbat.baseline.constraints;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import microbat.baseline.probpropagation.BeliefPropagation;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;

public class VariableConstraintA3Test {

	private VarValue readVar1;
	private VarValue readVar2;
	private VarValue writeVar1;
	private VarValue writeVar2;
	
	private TraceNode node;
	private TraceNode controlDom;
	
	private String controlDomValueID;
	
	private double propagationProbability;
	
	@Before
	public void init() {
		final String var1ID = "readVar1";
		LocalVar var1 = new LocalVar(var1ID, "int", "class", 2);
		var1.setVarID(var1ID);
		this.readVar1 = new PrimitiveValue("1", true, var1);
		
		final String var2ID = "readVar2";
		LocalVar var2 = new LocalVar(var2ID, "int", "class", 2);
		var2.setVarID(var2ID);
		this.readVar2 = new PrimitiveValue("2", true, var2);
		
		final String var3ID = "writeVar1";
		LocalVar var3 = new LocalVar(var3ID, "int", "class", 2);
		var3.setVarID(var3ID);
		this.writeVar1 = new PrimitiveValue("3", true, var3);
		
		final String var4ID = "writeVar2";
		LocalVar var4 = new LocalVar(var4ID, "int", "class", 2);
		var4.setVarID(var4ID);
		this.writeVar2 = new PrimitiveValue("4", true, var4);
		
		this.node = new TraceNode(null, null, 2, null, "");
		this.controlDom = new TraceNode(null, null, 1, null, "");
		
		final String type = "boolean";
		final String varName = BeliefPropagation.CONDITION_RESULT_NAME_PRE + this.controlDom.getOrder();
		
		this.controlDomValueID = BeliefPropagation.CONDITION_RESULT_ID_PRE + this.controlDom.getOrder();
		
		Variable variable = new LocalVar(varName, type, "", 1);
		VarValue conditionResult = new PrimitiveValue("1", true, variable);
		conditionResult.setVarID(this.controlDomValueID);
		this.controlDom.addWrittenVariable(conditionResult);
		
		this.propagationProbability = PropagationProbability.HIGH;
	}
	
	@After
	public void clearUp() {
		Constraint.resetID();
	}

	@Test
	public void testConstraintID() {
		this.node.addWrittenVariable(writeVar1);
		this.node.addReadVariable(readVar1);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint1 = new VariableConstraintA3(this.node, this.propagationProbability);
		Constraint constraint2 = new VariableConstraintA3(this.node, this.propagationProbability);
		Constraint constraint3 = new VariableConstraintA3(this.node, this.propagationProbability);
		
		assertEquals("VC3_0", constraint1.getConstraintID());
		assertEquals("VC3_1", constraint2.getConstraintID());
		assertEquals("VC3_2", constraint3.getConstraintID());
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void testMissingControlDominator() {
		this.node.addReadVariable(this.readVar1);
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void test0R0W1P() {
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
	}
	
	@Test
	public void testControlDominator() {
		this.node.addReadVariable(this.readVar1);
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		assertTrue(constraint.haveControlDom());
		assertEquals(this.controlDomValueID, constraint.getControlDomID());
	}
	
	@Test
	public void testVarIDs() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
		assertEquals(this.readVar2.getVarID(), predIDs.get(1));
		assertEquals(this.writeVar1.getVarID(), predIDs.get(2));
		assertEquals(this.controlDomValueID, predIDs.get(3));
	}
	
	@Test
	public void testNodeOrder() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		assertEquals(2, constraint.getOrder());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidCaseNo() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		constraint.getProbability(8);
	}
	
	@Test
	public void testDuplicatedVar() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar1);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		assertEquals(2, constraint.getBitLength());
		assertEquals(2, constraint.getPredicateCount());
		
		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
		assertEquals(this.controlDomValueID, predIDs.get(1));
	}
	
	@Test
	public void test2R0W1P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(3, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(2, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(3, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test
	public void test0R2W1P() {
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(3, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(2, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(3, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test
	public void test1R1W1P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(3, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(2, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(3, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test
	public void test2R1W1P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new VariableConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(4, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(3, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(4, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
}
