package microbat.debugpilot.propagation.BP.constraint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.model.value.VarValue;

public class VariableConstraintA2Test extends ConstraintTest {

    @Test
    public void testConstructionWithNullNode() {
        assertThrows(NullPointerException.class, () -> new VariableConstraintA2(null, this.readVar1));
    }
    
    @Test
    public void testConstructorWithInvalidPropbability() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(IllegalArgumentException.class, () -> new VariableConstraintA2(this.node, this.readVar1, -1.0d));
    }
    
    @Test
    public void testConstructorWithNullVar() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(NullPointerException.class, () -> new VariableConstraintA2(this.node, null));
    }
    
    
    @Test
    public void testConstructorWithInvalidVar() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new VariableConstraintA2(this.node, this.readVar2));
    }
    
    @Test
    public void test0R0W0C() {
        assertThrows(WrongConstraintConditionException.class, () -> new VariableConstraintA2(this.node, this.readVar1));
    }
    
    @Test
    public void test0R0W1C() {
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class, () -> new VariableConstraintA2(this.node, this.readVar1));
    }
    
    @Test
    public void test0R1W0C() {
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new VariableConstraintA2(this.node, this.readVar1));
    }
    
    @Test
    public void test0R1W1C() {
        this.node.addWrittenVariable(this.writeVar1);
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class, () -> new VariableConstraintA2(this.node, this.readVar1));
    }
    
    @Test
    public void test1R0W0C() {
        this.node.addReadVariable(this.readVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new VariableConstraintA2(this.node, this.readVar1));
    }
    
    @Test
    public void test1R0W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.setControlDominator(this.controlDom);
        Constraint constraint = new VariableConstraintA2(this.node, this.readVar1);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.node.getControlDominator().getConditionResult(), 2));
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        
        double[][] distribution = {
                {this.propagationProbability, this.propagationProbability},
                {1-this.propagationProbability, this.propagationProbability}
        };
        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("VA2_2", array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
    }
    
    @Test
    public void test1R1W0C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        Constraint constraint = new VariableConstraintA2(this.node, this.readVar1);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        
        double[][] distribution = {
                {this.propagationProbability, this.propagationProbability},
                {1-this.propagationProbability, this.propagationProbability}
        };
        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("VA2_2", array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
        
    }
    
    @Test
    public void tset1R1W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        this.node.setControlDominator(this.controlDom);
        Constraint constraint = new VariableConstraintA2(this.node, this.readVar1);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(new Variable<VarValue>(this.controlDom.getConditionResult(), 2));
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        
        double[][][] distribution = {
                {{this.propagationProbability, this.propagationProbability}, {this.propagationProbability, this.propagationProbability}},
                {{this.propagationProbability, this.propagationProbability}, {1 - this.propagationProbability, this.propagationProbability}}
        };
        
        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("VA2_2", array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
    }
}
