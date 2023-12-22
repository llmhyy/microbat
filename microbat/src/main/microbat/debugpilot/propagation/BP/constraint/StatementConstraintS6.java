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
 * StatementConstraintS6 is used to represent human knowledge about the statement is correct or not.
 * It is proposed to handle the statement that only have control dominator but no read or written
 * variables. <br/>
 * 
 * Constraint: <br/>
 * If the control dominator is correct, then the statement is correct. <br/>
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public class StatementConstraintS6 extends StatementConstraint {

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @see StatementConstraintS6#StatementConstraintS6(TraceNode, double)
     */
    public StatementConstraintS6(final TraceNode node) {
        this(node, PropProbability.HIGH);
    }

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * @throws WrongConstraintConditionException if the node does not satisfy the condition of
     *         StatementConstraintS6, which is: The node should only have one control dominator and
     *         no read and written variables.
     * 
     */
    public StatementConstraintS6(final TraceNode node, final double propagationProb) {
        super(node, genID(node), propagationProb);

        // Check that the node does not contain any read or written variables.
        if (!node.getReadVariables().isEmpty() || !node.getWrittenVariables().isEmpty()) {
            throw new WrongConstraintConditionException(
                    "Cannot form Statement Constraint S6 with any read or written variables for node: "
                            + node.getOrder());
        }

        // Check that the node has a control dominator.
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
        // Order: [controlDominator, statement]
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
