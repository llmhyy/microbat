package microbat.baseline.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintA3;
import microbat.debugpilot.propagation.BP.constraint.WrongConstraintConditionException;

public class StatementConstraintA3Test extends ConstraintTest {

	@Test
	public void testConstraintID() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);

		Constraint constraint1 = new StatementConstraintA3(this.node, this.propagationProbability);
		Constraint constraint2 = new StatementConstraintA3(this.node, this.propagationProbability);
		Constraint constraint3 = new StatementConstraintA3(this.node, this.propagationProbability);

		assertEquals("SC3_0", constraint1.getConstraintID());
		assertEquals("SC3_1", constraint2.getConstraintID());
		assertEquals("SC3_2", constraint3.getConstraintID());
	}

	@Test(expected = WrongConstraintConditionException.class)
	public void test0R0W1P() {
		this.node.setControlDominator(this.controlDom);
		@SuppressWarnings("unused")
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
	}

	@Test(expected = WrongConstraintConditionException.class)
	public void test1R0W0P() {
		this.node.addReadVariable(readVar1);
		@SuppressWarnings("unused")
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
	}

	@Test(expected = WrongConstraintConditionException.class)
	public void test0R1W0P() {
		this.node.addWrittenVariable(this.writeVar1);
		@SuppressWarnings("unused")
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCaseNo() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		constraint.getProbability(8);
	}

	@Test
	public void testControlDominator() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.setControlDominator(this.controlDom);

		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);

		assertTrue(constraint.haveControlDom());
		assertEquals(this.controlDomValueID, constraint.getControlDomID());
	}

	@Test
	public void testVarIDs() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
		this.node.setControlDominator(this.controlDom);

		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);

		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
		assertEquals(this.readVar2.getVarID(), predIDs.get(1));
		assertEquals(this.writeVar1.getVarID(), predIDs.get(2));
		assertEquals(this.writeVar2.getVarID(), predIDs.get(3));
		assertEquals(this.controlDomValueID, predIDs.get(4));
		assertEquals(this.statementID, predIDs.get(5));
	}

	@Test
	public void testNodeOrder() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);

		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		assertEquals(2, constraint.getOrder());
	}

	@Test
	public void testDuplicatedVar() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);

		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);

		assertEquals(3, constraint.getBitLength());
		assertEquals(3, constraint.getPredicateCount());

		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
		assertEquals(this.writeVar1.getVarID(), predIDs.get(1));
		assertEquals(this.statementID, predIDs.get(2));
	}
	
	@Test
	public void test1R1W0P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(3, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(2, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(3, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95, 0.95};
		
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
		
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(4, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(3, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(4, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.05, 0.95, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test
	public void test2R2W0P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
		
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(5, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(4, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(5, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.05, 0.95, 0.05, 0.95, 0.05, 0.95, 0.95};
		
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
		this.node.setControlDominator(controlDom);
		
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(4, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(3, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(4, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.05, 0.95, 0.95, 0.95, 0.95};
		
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
		
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(5, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(4, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(5, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.05, 0.95, 0.95, 0.95, 0.95};
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}
	
	@Test
	public void test2R2W1P() {
		this.node.addReadVariable(this.readVar1);
		this.node.addReadVariable(this.readVar2);
		this.node.addWrittenVariable(this.writeVar1);
		this.node.addWrittenVariable(this.writeVar2);
		this.node.setControlDominator(this.controlDom);
		
		Constraint constraint = new StatementConstraintA3(this.node, this.propagationProbability);
		
		// Test bit size
		assertEquals(6, constraint.getBitLength());
		
		// Test conclusion indexes
		assertEquals(5, constraint.getConclusionIdx());
		
		// Test count
		assertEquals(6, constraint.getPredicateCount());
		
		// Test probability
		double[] expected = new double[] {0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95, 0.95,
										  0.95, 0.95, 0.95, 0.05, 0.95, 0.95, 0.95, 0.05,
										  0.95, 0.95, 0.95, 0.05, 0.95, 0.95, 0.95, 0.95};
		
		
		final int totalLen = constraint.getPredicateCount();
		final int maxCase = 1 << totalLen;
		
		for (int caseNo=0; caseNo<maxCase; caseNo++) {
			assertEquals(expected[caseNo], constraint.getProbability(caseNo), 0.01);
		}
	}

}
