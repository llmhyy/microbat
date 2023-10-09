package microbat.debugpilot.userfeedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;

public class DPUserFeedbackTest {

	protected VarValue var1;
	protected VarValue var2;
	protected VarValue var3;
	protected VarValue var4;
	
	protected TraceNode node1;
	protected TraceNode node2;
	
	@Before
	public void init() {
		final String var1ID = "readVar1";
		LocalVar var1 = new LocalVar(var1ID, "int", "class", 2);
		var1.setVarID(var1ID);
		this.var1 = new PrimitiveValue("1", true, var1);
		
		final String var2ID = "readVar2";
		LocalVar var2 = new LocalVar(var2ID, "int", "class", 2);
		var2.setVarID(var2ID);
		this.var2 = new PrimitiveValue("2", true, var2);
		
		final String var3ID = "writeVar1";
		LocalVar var3 = new LocalVar(var3ID, "int", "class", 2);
		var3.setVarID(var3ID);
		this.var3 = new PrimitiveValue("3", true, var3);
		
		final String var4ID = "writeVar2";
		LocalVar var4 = new LocalVar(var4ID, "int", "class", 2);
		var4.setVarID(var4ID);
		this.var4 = new PrimitiveValue("4", true, var4);
		
		this.node1 = new TraceNode(null, null, 2, null, "");
		this.node2 = new TraceNode(null, null, 1, null, "");
	}
	
	@Test
	public void testConstructor1() {
		DPUserFeedback feedback1 = new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, this.node1);
		assertEquals(DPUserFeedbackType.ROOT_CAUSE, feedback1.getType());
		assertEquals(this.node1, feedback1.getNode());
		
		assertThrows(NullPointerException.class, ()-> {
			@SuppressWarnings("unused")
			DPUserFeedback feedback2 = new DPUserFeedback(null, node1);
		});
		
		assertThrows(NullPointerException.class, ()-> {
			@SuppressWarnings("unused")
			DPUserFeedback feedback2 = new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, null);
		});
		
		assertThrows(NullPointerException.class, ()-> {
			@SuppressWarnings("unused")
			DPUserFeedback feedback2 = new DPUserFeedback(null, null);
		});
	}
	
	@Test
	public void testConstructor2() {
		Set<VarValue> wrongVars = new HashSet<>();
		wrongVars.add(var1);
		wrongVars.add(var2);
		
		Set<VarValue> correctVars = new HashSet<>();
		correctVars.add(var3);
		correctVars.add(var4);
		
		DPUserFeedback feedback1 = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, this.node1, wrongVars, correctVars);
		assertEquals(DPUserFeedbackType.WRONG_VARIABLE, feedback1.getType());
		assertEquals(this.node1, feedback1.getNode());
		assertEquals(wrongVars, feedback1.getWrongVars());
		assertEquals(correctVars, feedback1.getCorrectVars());
		
		assertThrows(NullPointerException.class, ()-> {
			@SuppressWarnings("unused")
			DPUserFeedback feedback2 = new DPUserFeedback(null, node1, null, null);
		});
		
		assertThrows(NullPointerException.class, ()-> {
			@SuppressWarnings("unused")
			DPUserFeedback feedback2 = new DPUserFeedback(DPUserFeedbackType.ROOT_CAUSE, null, wrongVars, null);
		});
		
		assertThrows(NullPointerException.class, ()-> {
			@SuppressWarnings("unused")
			DPUserFeedback feedback2 = new DPUserFeedback(null, null, null, correctVars);
		});
	}
	
	@Test
	public void testAddWrongVariables() {
		List<VarValue> wrongVars = new ArrayList<>();
		wrongVars.add(this.var1);
		List<VarValue> correctVars = new ArrayList<>();
		correctVars.add(this.var2);
		correctVars.add(this.var3);
		
		DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, this.node1, wrongVars, correctVars);
		feedback.addWrongVar(this.var3);
		
		Set<VarValue> expectedWrongVars = new HashSet<VarValue>();
		expectedWrongVars.add(this.var1);
		expectedWrongVars.add(this.var3);
		
		Set<VarValue> expectedCorrectVars = new HashSet<VarValue>();
		expectedCorrectVars.add(this.var2);
	
		assertEquals(expectedWrongVars, feedback.getWrongVars());
		assertEquals(expectedCorrectVars, feedback.getCorrectVars());
	}
	
	@Test
	public void testAddCorrectVariables() {
		List<VarValue> wrongVars = new ArrayList<>();
		wrongVars.add(this.var1);
		wrongVars.add(this.var2);
		List<VarValue> correctVars = new ArrayList<>();
		correctVars.add(this.var3);
		
		DPUserFeedback feedback = new DPUserFeedback(DPUserFeedbackType.WRONG_VARIABLE, this.node1, wrongVars, correctVars);
		feedback.addCorrectVar(this.var2);
		
		Set<VarValue> expectedWrongVars = new HashSet<VarValue>();
		expectedWrongVars.add(this.var1);
		
		Set<VarValue> expectedCorrectVars = new HashSet<VarValue>();
		expectedCorrectVars.add(this.var2);
		expectedCorrectVars.add(this.var3);
	
		assertEquals(expectedWrongVars, feedback.getWrongVars());
		assertEquals(expectedCorrectVars, feedback.getCorrectVars());
	}
	
}
