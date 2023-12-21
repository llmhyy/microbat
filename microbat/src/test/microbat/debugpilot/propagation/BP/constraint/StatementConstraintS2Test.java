package microbat.debugpilot.propagation.BP.constraint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementConstraintS2Test extends ConstraintTest {

    @Test
    public void testConstructionWithNullNode() {
        assertThrows(NullPointerException.class, () -> new StatementConstraintS2(null));
    }
    
    @Test
    public void testConstructorWithInvalidPropbability() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(IllegalArgumentException.class, () -> new StatementConstraintS2(this.node, -1.0d));
    }

    @Test
    public void test0R0W0C() {
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS2(this.node));
    }
    
    @Test
    public void test0R0W1C() {
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS2(this.node));
    }
    
    @Test
    public void test0R1W0C() {
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS2(this.node));
    }
    
    @Test
    public void test0R1W1C() {
        this.node.addWrittenVariable(this.writeVar1);
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS2(this.node));
    }
    
    @Test
    public void test1R0W0C() {
        this.node.addReadVariable(this.readVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS2(this.node));
    }
    
    @Test
    public void test1R0W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS2(this.node));
    }
    
    @Test
    public void test1R1W0C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        Constraint constraint = new StatementConstraintS2(this.node);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        
        double[][][] distribution = {
                {{1-this.propagationProbability, this.propagationProbability}, {this.propagationProbability, this.propagationProbability}},
                {{this.propagationProbability, this.propagationProbability}, {this.propagationProbability, this.propagationProbability}}
        };
        
        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS2_2", array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
    }
    
    @Test
    public void test1R1W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        this.node.setControlDominator(this.controlDom);
        Constraint constraint = new StatementConstraintS2(this.node);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(new Variable<VarValue>(this.controlDom.getConditionResult(), 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        double[][][][] distribution = {
                {{{this.propagationProbability, this.propagationProbability}, {1-this.propagationProbability, this.propagationProbability}},
                {{this.propagationProbability, this.propagationProbability}, {this.propagationProbability, this.propagationProbability}}},
                {{{this.propagationProbability, this.propagationProbability}, {this.propagationProbability, this.propagationProbability}},
                {{this.propagationProbability, this.propagationProbability}, {this.propagationProbability, this.propagationProbability}}},
        };
        
        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS2_2", array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
    }
    
    @Test
    public void test2R2W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addReadVariable(this.readVar2);
        this.node.addWrittenVariable(this.writeVar1);
        this.node.addWrittenVariable(this.writeVar2);
        this.node.setControlDominator(this.controlDom);
        Constraint constraint = new StatementConstraintS2(this.node);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        variables.add(new Variable<VarValue>(this.readVar2, 2));
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(new Variable<VarValue>(this.writeVar2, 2));
        variables.add(new Variable<VarValue>(this.controlDom.getConditionResult(), 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        
        int[] shape = IntStream.generate(() -> 2).limit(6).toArray();
        HDArray array = HDArray.createBySizeWithValue(propagationProbability, shape);
        
        
        array.set(1-this.propagationProbability, new int[] {0,0,0,0,1,0});
        array.set(1-this.propagationProbability, new int[] {0,0,0,1,1,0});
        array.set(1-this.propagationProbability, new int[] {0,0,1,0,1,0});
        array.set(1-this.propagationProbability, new int[] {0,1,0,0,1,0});
        array.set(1-this.propagationProbability, new int[] {0,1,0,1,1,0});
        array.set(1-this.propagationProbability, new int[] {0,1,1,0,1,0});
        array.set(1-this.propagationProbability, new int[] {1,0,0,0,1,0});
        array.set(1-this.propagationProbability, new int[] {1,0,0,1,1,0});
        array.set(1-this.propagationProbability, new int[] {1,0,1,0,1,0});
        
        Factor expectedFactor = new Factor("SS2_2", array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
        
    }
}
