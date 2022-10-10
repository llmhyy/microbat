package microbat.baseline.constraints;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import microbat.baseline.encoders.BeliefPropagation;
import microbat.baseline.factorgraph.MessageProcessor;
import microbat.baseline.factorgraph.VarIDConverter;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;

public class MessageProcessorTest {

	private VarValue readVar1;
	private VarValue readVar2;
	private VarValue writeVar1;
	private VarValue writeVar2;
	
	private TraceNode node;
	private TraceNode controlDom;
	
	private String controlDomValueID;
	
	private double propagationProbability;
	private String statementID;
	
	private VarIDConverter converter;
	private MessageProcessor processor;
	
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
		this.statementID = "S_2";
		
		this.converter = new VarIDConverter();
		this.processor = new MessageProcessor();
	}

	@After
	public void tearDown() throws Exception {
		Constraint.resetID();
	}

	@Test
	public void testGraphMessage() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		
		Constraint constraint1 = new VariableConstraintA1(this.node, this.writeVar1, this.propagationProbability);
		Constraint constraint2 = new VariableConstraintA2(this.node, this.readVar1, this.propagationProbability);
		
		List<Constraint> constraints = new ArrayList<>();
		constraints.add(constraint1);
		constraints.add(constraint2);
		
		final String expected = "VC1_0(" + this.readVar1.getVarID() + "," + this.writeVar1.getVarID() + ")VC2_0(" + this.readVar1.getVarID() + "," + this.writeVar1.getVarID() + ")";
		assertEquals(expected, this.processor.buildGraphMsg(constraints));
	}
	
	@Test
	public void testFactorMessage() {
		this.node.addReadVariable(this.readVar1);
		this.node.addWrittenVariable(this.writeVar1);
		
		Constraint constraint1 = new VariableConstraintA1(this.node, this.writeVar1, this.propagationProbability);
		Constraint constraint2 = new VariableConstraintA2(this.node, this.readVar1, this.propagationProbability);
		
		List<Constraint> constraints = new ArrayList<>();
		constraints.add(constraint1);
		constraints.add(constraint2);
		
		final String expected = "2&VC1_0&" + this.readVar1.getVarID() + "," + this.writeVar1.getVarID() + "&0.95*2,0.05*1,0.95*1&" +
								"2&VC2_0&" + this.readVar1.getVarID() + "," + this.writeVar1.getVarID() + "&0.95*1,0.05*1,0.95*2";
		assertEquals(expected, this.processor.buildFactorMsg(constraints));
	}

}
