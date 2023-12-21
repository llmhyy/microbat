package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementConstraintS6 extends StatementConstraint {

    public StatementConstraintS6(final TraceNode node) {
        this(node, PropProbability.HIGH);
    }

    public StatementConstraintS6(final TraceNode node, final double propagationProb) {
        super(node, genID(node), propagationProb);

        if (!node.getReadVariables().isEmpty() || !node.getWrittenVariables().isEmpty()) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S6 with any read or written variables for node: "
                            + node.getOrder());
        }

        if (node.getControlDominator() == null) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S6 without control dominator for node: "
                            + node.getOrder());
        }
    }

    @Override
    public Factor genFactor() {
        /*
         * Invalid case is that 1. Control dominator is correct 2. Statement is wrong
         */

        final String factorName = this.name;

        // Collect variables. Note that the order matters.
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(
                new Variable<VarValue>(this.node.getControlDominator().getConditionResult(), 2));
        variables.add(new Variable<TraceNode>(this.node, 2));

        // Calculate probability distribution
        final int numPredicates = variables.size();
        final int[] shape = Constraint.initShapeByDimension(numPredicates);
        final HDArray array = HDArray.createBySizeWithValue(this.propagationProb, shape);
        for (int[] indices : this.genAllPossibleIndices(numPredicates)) {
            if (!this.haveWrongControlDominator(indices) && this.haveWrongStatement(indices)) {
                array.set(1 - this.propagationProb, indices);
            }
        }

        return new Factor(factorName, array, variables);
    }

    private static String genID(final TraceNode node) {
        return "SS6_" + node.getOrder();
    }
}
