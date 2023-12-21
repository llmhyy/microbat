package microbat.debugpilot.propagation.BP.constraint;

import org.junit.Before;

import microbat.model.BreakPoint;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ConditionVar;
import microbat.model.variable.LocalVar;

public abstract class ConstraintTest {
    
    protected VarValue readVar1;
    protected VarValue readVar2;
    protected VarValue writeVar1;
    protected VarValue writeVar2;
    
    protected TraceNode node;
    protected TraceNode controlDom;
    
    protected VarValue controlConditionResult;
    
    protected double propagationProbability;
    
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
        
        this.controlDom = new TraceNode(null, null, 1, null, "");
        BreakPoint breakpoint = new BreakPoint("", "", 2);
        breakpoint.setBranch(true);
        this.controlDom.setBreakPoint(breakpoint);
        
        LocalVar conditionVar = new ConditionVar(1, 1);
        this.controlConditionResult = new PrimitiveValue("5", true, conditionVar);
        this.controlConditionResult.setVarID(ConditionVar.CONDITION_RESULT_ID + this.controlDom.getOrder());
        this.controlDom.addWrittenVariable(this.controlConditionResult);
        
        this.node = new TraceNode(null, null, 2, null, "");
        BreakPoint breakpoint2 = new BreakPoint("", "", 2);
        this.node.setBreakPoint(breakpoint2);
        
        this.propagationProbability = 0.95;
    }
}
