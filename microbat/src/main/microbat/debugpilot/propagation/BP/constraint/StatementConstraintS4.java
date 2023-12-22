package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * StatementConstraintS4 is used to represent human knowledge about the statement is correct or not.
 * It is the constraint proposed to handle the statement that only have control dominator and either
 * read or written variables but not both. <br/>
 * 
 * Constraint: <br/>
 * Under the situation that control dominator is correct, the statement is wrong if the read/written
 * variables is wrong. <br/>
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public class StatementConstraintS4 extends StatementConstraint {

    /**
     * The variable that the constraint is associated with.
     */
    protected final VarValue var;

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param var The variable that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @see StatementConstraintS4#StatementConstraintS4(TraceNode, VarValue, double)
     */
    public StatementConstraintS4(final TraceNode node, final VarValue var) {
        this(node, var, PropProbability.HIGH);
    }

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param var The variable that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * @throws WrongConstraintConditionException if the node does not satisfy the condition of
     *         StatementConstraintS4, which is: 1. The node has at least one predicate. 2. The node
     *         contain either read or written variable but not both.
     * 
     */
    public StatementConstraintS4(final TraceNode node, final VarValue var,
            final double propagationProb) {
        super(node, genID(node), propagationProb);

        // Check that node should have at least one read or written variables.
        if (node.getReadVariables().isEmpty() && node.getWrittenVariables().isEmpty()) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S4 without any read or written variables for node: "
                            + node.getOrder());
        }

        // Check that node should not have both read and written variables.
        if (!node.getReadVariables().isEmpty() && !node.getWrittenVariables().isEmpty()) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S4 with both read and written variables for node: "
                            + node.getOrder());
        }

        // Check that node should have the given variable.
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
        // Order: [target variable, control dominator, statement]
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

    /**
     * Check if the given indices represent wrong target variable.
     * 
     * @param indices The given indices.
     * @return True if the given indices represent wrong target variable, false otherwise.
     */
    protected boolean haveWrongTargetVariable(final int[] indices) {
        return indices[0] == 0;
    }

    private static String genID(final TraceNode node) {
        return "SS4_" + node.getOrder();
    }

}
