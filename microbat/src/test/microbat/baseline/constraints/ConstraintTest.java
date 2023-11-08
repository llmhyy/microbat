package microbat.baseline.constraints;

import org.junit.After;
import org.junit.Before;

import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ConditionVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;

public abstract class ConstraintTest {

	protected VarValue readVar1;
	protected VarValue readVar2;
	protected VarValue writeVar1;
	protected VarValue writeVar2;
	
	protected TraceNode node;
	protected TraceNode controlDom;
	
	protected String controlDomValueID;
	
	protected double propagationProbability;
	protected String statementID;
	
	
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
		final String varName = ConditionVar.CONDITION_RESULT_NAME + this.controlDom.getOrder();
		
		this.controlDomValueID = ConditionVar.CONDITION_RESULT_ID + this.controlDom.getOrder();
		
		Variable variable = new ConditionVar(varName, type, "", 1);
		VarValue conditionResult = new PrimitiveValue("1", true, variable);
		conditionResult.setVarID(this.controlDomValueID);
		this.controlDom.addWrittenVariable(conditionResult);
		
		this.propagationProbability = PropProbability.HIGH;
		this.statementID = "S_2";
	}

	@After
	public void tearDown() throws Exception {
		Constraint.resetID();
	}
	
}
