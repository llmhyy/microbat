package microbat.debugpilot.propagation.BP.constraint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementConstraintS4Test extends ConstraintTest {
    @Test
    public void testConstructionWithNullNode() {
        assertThrows(NullPointerException.class,
                () -> new StatementConstraintS4(null, this.readVar1));
    }

    @Test
    public void testConstructorWithInvalidPropbability() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(IllegalArgumentException.class,
                () -> new StatementConstraintS4(this.node, this.readVar1, -1.0d));
    }

    @Test
    public void testConstructorWithInvalidVariable() {
        this.node.addReadVariable(readVar1);
        assertThrows(WrongConstraintConditionException.class,
                () -> new StatementConstraintS4(this.node, this.writeVar1));
    }

    @Test
    public void test0R0W0C() {
        assertThrows(WrongConstraintConditionException.class,
                () -> new StatementConstraintS4(this.node, this.readVar1));
    }

    @Test
    public void test0R0W1C() {
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class,
                () -> new StatementConstraintS4(this.node, this.readVar1));
    }

    @Test
    public void test0R1W0C() {
        this.node.addWrittenVariable(this.writeVar1);
        Constraint constraint = new StatementConstraintS4(this.node, this.writeVar1);

        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        double[][] distribution = {{this.propagationProbability, 1 - this.propagationProbability},
                {this.propagationProbability, this.propagationProbability}};

        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS4_2", array, variables);

        assertEquals(expectedFactor, constraint.genFactor());
    }

    @Test
    public void test0R1W1C() {
        this.node.addWrittenVariable(this.writeVar1);
        this.node.setControlDominator(this.controlDom);
        Constraint constraint = new StatementConstraintS4(this.node, this.writeVar1);

        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.writeVar1, 2));
        variables.add(
                new Variable<VarValue>(this.node.getControlDominator().getConditionResult(), 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        double[][][] distribution = {
                {{this.propagationProbability, this.propagationProbability},
                        {this.propagationProbability, 1 - this.propagationProbability}},
                {{this.propagationProbability, this.propagationProbability},
                        {this.propagationProbability, this.propagationProbability}},};

        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS4_2", array, variables);

        assertEquals(expectedFactor, constraint.genFactor());
    }

    @Test
    public void test1R0W0C() {
        this.node.addReadVariable(this.readVar1);
        Constraint constraint = new StatementConstraintS4(this.node, this.readVar1);

        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        double[][] distribution = {{this.propagationProbability, 1 - this.propagationProbability},
                {this.propagationProbability, this.propagationProbability}};

        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS4_2", array, variables);

        assertEquals(expectedFactor, constraint.genFactor());
    }

    @Test
    public void test1R0W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.setControlDominator(this.controlDom);
        Constraint constraint = new StatementConstraintS4(this.node, this.readVar1);

        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        variables.add(
                new Variable<VarValue>(this.node.getControlDominator().getConditionResult(), 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        double[][][] distribution = {
                {{this.propagationProbability, this.propagationProbability},
                        {this.propagationProbability, 1 - this.propagationProbability}},
                {{this.propagationProbability, this.propagationProbability},
                        {this.propagationProbability, this.propagationProbability}},};

        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS4_2", array, variables);

        assertEquals(expectedFactor, constraint.genFactor());
    }

    @Test
    public void test1R1W0C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        assertThrows(WrongConstraintConditionException.class,
                () -> new StatementConstraintS4(this.node, this.readVar1));
    }

    @Test
    public void test1R1W1C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addWrittenVariable(this.writeVar1);
        this.node.setControlDominator(this.controlDom);
        assertThrows(WrongConstraintConditionException.class,
                () -> new StatementConstraintS4(this.node, this.readVar1));
    }

    @Test
    public void test2R0W0C() {
        this.node.addReadVariable(this.readVar1);
        this.node.addReadVariable(this.readVar2);

        Constraint constraint = new StatementConstraintS4(this.node, this.readVar1);

        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<VarValue>(this.readVar1, 2));
        variables.add(new Variable<TraceNode>(this.node, 2));
        double[][] distribution = {{this.propagationProbability, 1 - this.propagationProbability},
                {this.propagationProbability, this.propagationProbability}};

        HDArray array = HDArray.create(distribution);
        Factor expectedFactor = new Factor("SS4_2", array, variables);

        assertEquals(expectedFactor, constraint.genFactor());

    }
}
