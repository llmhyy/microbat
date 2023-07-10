package microbat.baseline.constraints;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.PriorConstraint;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;

public class PriorConstraintTest {

	private VarValue readVar1;
	private double propagationProbability;
	
	@Before
	public void init() {
		final String var1ID = "readVar1";
		LocalVar var1 = new LocalVar(var1ID, "int", "class", 2);
		var1.setVarID(var1ID);
		this.readVar1 = new PrimitiveValue("1", true, var1);
		
		this.propagationProbability = PropProbability.HIGH;
	}
	

	@After
	public void tearDown() throws Exception {
		Constraint.resetID();
	}

	@Test
	public void testConstraintID() {
		
		Constraint constraint1 = new PriorConstraint(this.readVar1, this.propagationProbability);
		Constraint constraint2 = new PriorConstraint(this.readVar1, this.propagationProbability);
		Constraint constraint3 = new PriorConstraint(this.readVar1, this.propagationProbability);
		
		assertEquals("PC_0", constraint1.getConstraintID());
		assertEquals("PC_1", constraint2.getConstraintID());
		assertEquals("PC_2", constraint3.getConstraintID());
	}
	
	@Test
	public void testVarIDs() {
		Constraint constraint = new PriorConstraint(this.readVar1, this.propagationProbability);
		
		List<String> predIDs = constraint.getInvolvedPredIDs();
		assertEquals(this.readVar1.getVarID(), predIDs.get(0));
	}
	
	@Test
	public void testNodeOrder() {
		Constraint constraint = new PriorConstraint(this.readVar1, this.propagationProbability);
		assertEquals(-1, constraint.getOrder());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidCaseNo() {
		Constraint constraint = new PriorConstraint(this.readVar1, this.propagationProbability);
		constraint.getProbability(2);
	}
	
	@Test
	public void testProbability() {
		Constraint constraint = new PriorConstraint(this.readVar1, this.propagationProbability);
		assertEquals(0.05, constraint.getProbability(0), 0.001);
		assertEquals(0.95, constraint.getProbability(1), 0.001);
	}
}
