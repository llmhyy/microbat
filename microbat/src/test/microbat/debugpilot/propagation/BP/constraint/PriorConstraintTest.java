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

public class PriorConstraintTest extends ConstraintTest {
    @Test
    public void testConstructionWithNullNode() {
        assertThrows(NullPointerException.class, () -> new StatementConstraintS5(null, this.readVar1));
    }
    
    @Test
    public void testConstructorWithInvalidPropbability() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(IllegalArgumentException.class, () -> new StatementConstraintS5(this.node, this.readVar1, -1.0d));
    }
    
    @Test
    public void testConstructorWithNullVariable() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS5(this.node, null));
    }
    
    @Test
    public void testConstructorWithInvalidVariable() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(WrongConstraintConditionException.class, () -> new StatementConstraintS5(this.node, this.readVar2));
    }
    
    @Test
    public void testGenFactor() {
        this.node.addReadVariable(this.readVar1);
        Constraint constraint = new PriorConstraint(this.node, this.readVar1, this.propagationProbability);
        
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        
        double[] distribution = {
                1-this.propagationProbability, this.propagationProbability
        };
        
        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("P_2_" + this.readVar1.getVarID(), array, variables);
        
        assertEquals(expectedFactor, constraint.genFactor());
    }
}
