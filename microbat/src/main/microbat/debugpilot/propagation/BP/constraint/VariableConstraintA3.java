package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.log.Log;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class VariableConstraintA3 extends VariableConstraint {

    protected final VarValue controlDom;

    public VariableConstraintA3(final TraceNode node) {
        this(node, PropProbability.HIGH);
    }

    public VariableConstraintA3(final TraceNode node, final double propagtionProbability) {
        super(node, VariableConstraintA3.genID(node), propagtionProbability);

        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A3 without any predicates for node: "
                            + node.getOrder()));
        }

        if (node.getControlDominator() == null) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A3 without control dominator for node: "
                            + node.getOrder()));
        }

        if (Constraint.countPredicates(node) == 1) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A3 without any written variables and read variables for node: "
                            + node.getOrder()));
        }

        this.controlDom = node.getControlDominator().getConditionResult();
    }

    @Override
    public Factor genFactor() {
        // For VA3, the invalid case is that all the other written variables and read variables
        // are correct, but the control dominator is wrong
        final String factorName = this.name;

        // Construct variables for factor, note that order matters
        List<Variable<?>> variables = new ArrayList<>();
        for (VarValue varValue : node.getReadVariables()) {
            variables.add(new Variable<>(varValue, 2));
        }
        for (VarValue varValue : node.getWrittenVariables()) {
            variables.add(new Variable<>(varValue, 2));
        }
        variables.add(new Variable<>(this.controlDom, 2));

        // Calculate probability distribution
        final int numPredicates = variables.size();
        final int[] shape = Constraint.initShapeByDimension(numPredicates);
        HDArray probabilityDistribution =
                HDArray.createBySizeWithValue(this.propagationProb, shape);
        final int[] invalidIndices = this.genInvalidIndices(numPredicates);
        probabilityDistribution.set(1.0d - this.propagationProb, invalidIndices);

        return new Factor(factorName, probabilityDistribution, variables);
    }

    private static String genID(final TraceNode node) {
        return "VA3_" + node.getOrder();
    }

}
