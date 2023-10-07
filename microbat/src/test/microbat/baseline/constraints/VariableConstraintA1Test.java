package microbat.baseline.constraints;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.VariableConstraintA1;
import microbat.debugpilot.propagation.BP.constraint.VariableConstraintA2;
import microbat.debugpilot.propagation.BP.constraint.WrongConstraintConditionException;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ConditionVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
@SuppressWarnings("unused")
public class VariableConstraintA1Test extends ConstraintTest {

	@Test
	public void testConstraintID() {
		this.node.addWrittenVariable(writeVar1);
		this.node.addReadVariable(readVar1);
		
		VariableConstraintA1 constraint1 = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		VariableConstraintA1 constraint2 = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		VariableConstraintA1 constraint3 = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
		assertEquals("VC1_0", constraint1.getConstraintID());
		assertEquals("VC1_1", constraint2.getConstraintID());
		assertEquals("VC1_2", constraint3.getConstraintID());
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void test0R0W0P() {
		VariableConstraintA1 constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void testUnMatchWrittenVariable() {
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addReadVariable(this.readVar1);
		VariableConstraintA1 constraint = new VariableConstraintA1(this.node, this.writeVar2, PropProbability.HIGH);
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void testMissingReadVars() {
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addControlDominatee(this.controlDom);
		VariableConstraintA1 constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
	}
	
//	@Test
//	public void testControlDominator() {
//		this.node.addWrittenVariable(this.writeVar1);
//		this.node.setControlDominator(this.controlDom);
//		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
//		
//		assertTrue(constraint.haveControlDom());
//		assertEquals(this.controlDomValueID, constraint.getControlDomID());
//	}
	
	@Test
	public void testVarIDs() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
//		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
		assertEquals(this.readVar2.getVarID(), predIDs.get(1));
		assertEquals(this.writeVar1.getVarID(), predIDs.get(2));
//		assertEquals(this.controlDomValueID, predIDs.get(3));
	}
	
	@Test 
	public void testNodeOrder() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
//		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
		assertEquals(2, constraint.getOrder());
	}
	
//	@Test
//	public void test0R1W1P() {
//		this.node.addWrittenVariable(this.writeVar1);
//		this.node.setControlDominator(this.controlDom);
//		VariableConstraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
//		
//		// Test bit size
//		assertEquals(2, constraint.getBitLength());
//		
//		// Test conclusion indexes
//		assertEquals(0, constraint.getConclusionIdx());
//		
//		// Test count
//		assertEquals(2, constraint.getPredicateCount());
//		
//		// Test probability
//		double[] expected = new double[] {0.95, 0.05, 0.95, 0.95};
//		
//		final int totalLen = constraint.getPredicateCount();
//		final int maxCase = 1 << totalLen;
//		
//		for (int caseNo=0; caseNo<maxCase; caseNo++) {
//			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
//		}
//	}
	
	@Test
	public void test1R1W0P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		
		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
		// Test bit size
		assertEquals(2, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(1, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(2, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.05, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test
	public void test2R1W0P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		
		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
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
	
//	@Test
//	public void test1R1W1P() {
//		this.node.addReadVariable(this.readVar1);
//		this.node.addWrittenVariable(this.writeVar1);
//		this.node.setControlDominator(this.controlDom);
//		
//		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
//		
//		// Test bit size
//		assertEquals(3, constraint.getBitLength());
//		
//		// Test conclusion indexes
//		assertEquals(1, constraint.getConclusionIdx());
//		
//		// Test count
//		assertEquals(3, constraint.getPredicateCount());
//		
//		// Test probability
//		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95, 0.95};
//		
//		final int totalLen = constraint.getPredicateCount();
//		final int maxCase = 1 << totalLen;
//		
//		for (int caseNo=0; caseNo<maxCase; caseNo++) {
//			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
//		}
//	}
	
//	@Test
//	public void test2R1W1P() {
//		this.node.addReadVariable(this.readVar1);
//		this.node.addReadVariable(this.readVar2);
//		this.node.addWrittenVariable(this.writeVar1);
//		this.node.setControlDominator(this.controlDom);
//		
//		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
//		
//		// Test bit size
//		assertEquals(4, constraint.getBitLength());
//		
//		// Test conclusion indexes
//		assertEquals(2, constraint.getConclusionIdx());
//		
//		// Test count
//		assertEquals(4, constraint.getPredicateCount());
//		
//		// Test probability
//		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95, 0.95};
//		
//		final int totalLen = constraint.getPredicateCount();
//		final int maxCase = 1 << totalLen;
//		
//		for (int caseNo=0; caseNo<maxCase; caseNo++) {
//			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
//		}
//	}
	
	@Test
	public void testDuplicatedVar() {
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addReadVariable(this.readVar1);
		
		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
		assertEquals(2, constraint.getBitLength());
		assertEquals(2, constraint.getPredicateCount());
		
		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
		assertEquals(this.writeVar1.getVarID(), predIDs.get(1));
	}
	
	@Test
	public void test1R2W0P() {
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
		this.node.addReadVariable(this.readVar1);
		
		Constraint constraint = new VariableConstraintA1(this.node, this.writeVar1, PropProbability.HIGH);
		
		// Test bit size
		assertEquals(2, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(1, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(2, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.05, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidCaseNo() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		
		Constraint constraint = new VariableConstraintA2(this.node, this.readVar1, PropProbability.HIGH);
		
		constraint.getProbability(4);
	}
	
}
