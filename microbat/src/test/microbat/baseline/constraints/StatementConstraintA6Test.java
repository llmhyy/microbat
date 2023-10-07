package microbat.baseline.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA6;
import microbat.debugpilot.propagation.BP.constraint.WrongConstraintConditionException;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ConditionVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
@SuppressWarnings("unused")
public class StatementConstraintA6Test extends ConstraintTest {

	@Test
	public void testConstraintID() {
		this.node.setControlDominator(this.controlDom);

		Constraint constraint1 = new StatementConstraintA6(this.node, this.propagationProbability);
		Constraint constraint2 = new StatementConstraintA6(this.node, this.propagationProbability);
		Constraint constraint3 = new StatementConstraintA6(this.node, this.propagationProbability);

		assertEquals("SC6_0", constraint1.getConstraintID());
		assertEquals("SC6_1", constraint2.getConstraintID());
		assertEquals("SC6_2", constraint3.getConstraintID());
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void test0R0W0P() {
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void test1R0W0P() {
		this.node.addReadVariable(this.readVar1);
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void test0R1W0P() {
		this.node.addWrittenVariable(this.writeVar1);
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
	}
	
	@Test(expected=WrongConstraintConditionException.class)
	public void test1R1W0P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
	}
	
	@Test
	public void testControlDominator() {
		this.node.setControlDominator(this.controlDom);

		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);

		assertTrue(constraint.haveControlDom());
		assertEquals(this.controlDomValueID, constraint.getControlDomID());
	}
	
	@Test
	public void testVarIDs() {
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
		
		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.controlDomValueID, predIDs.get(0));
		assertEquals(this.statementID, predIDs.get(1));
	}
	
	@Test
	public void testNodeOrder() {
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
		assertEquals(2, constraint.getOrder());
	}
	
	@Test
	public void test0R0W1P() {
		this.node.setControlDominator(this.controlDom);
		Constraint constraint = new StatementConstraintA6(this.node, this.propagationProbability);
		
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
}
