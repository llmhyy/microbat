package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementConstraintS4 extends StatementConstraint {

    protected final VarValue var;

    public StatementConstraintS4(final TraceNode node, final VarValue var) {
        this(node, var, PropProbability.HIGH);
    }

    public StatementConstraintS4(final TraceNode node, final VarValue var,
            final double propagationProb) {
        super(node, genID(node), propagationProb);

        if (node.getReadVariables().isEmpty() && node.getWrittenVariables().isEmpty()) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S4 without any read or written variables for node: "
                            + node.getOrder());
        }

        if (!node.getReadVariables().isEmpty() && !node.getWrittenVariables().isEmpty()) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S4 with both read and written variables for node: "
                            + node.getOrder());
        }

        if (!node.isReadVariablesContains(var.getVarID())
                && !node.isWrittenVariablesContains(var.getVarID())) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S4 without the given variable for node: "
                            + node.getOrder());
        }

        this.var = var;
    }

    @Override
    public Factor genFactor() {
        /*
         * Invalid case is that: 1. Control dominator is correct 2. Target variable is wrong 3.
         * Statement is correct
         */

        final String factorName = this.name;

        // Collect variables. Note that the order matters.
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<>(this.var, 2));
        if (this.node.getControlDominator() != null) {
            variables.add(new Variable<VarValue>(
                    this.node.getControlDominator().getConditionResult(), 2));
        }
        variables.add(new Variable<TraceNode>(this.node, 2));

        // Calculate probability distribution
        final int numPredicates = variables.size();
        final int[] shape = Constraint.initShapeByDimension(numPredicates);
        HDArray probabilityDistribution =
                HDArray.createBySizeWithValue(this.propagationProb, shape);
        if (node.getControlDominator() == null) {
            for (int[] indices : this.genAllPossibleIndices(numPredicates)) {
                if (this.haveWrongTargetVariable(indices) && !this.haveWrongStatement(indices)) {
                    probabilityDistribution.set(1 - this.propagationProb, indices);
                }
            }
        } else {
            for (int[] indices : this.genAllPossibleIndices(numPredicates)) {
                if (this.haveWrongTargetVariable(indices) && !this.haveWrongStatement(indices)
                        && !this.haveWrongControlDominator(indices)) {
                    probabilityDistribution.set(1 - this.propagationProb, indices);
                }
            }
        }

        return new Factor(factorName, probabilityDistribution, variables);
    }

    protected boolean haveWrongTargetVariable(final int[] indices) {
        return indices[0] == 0;
    }

    private static String genID(final TraceNode node) {
        return "SS4_" + node.getOrder();
    }

}
