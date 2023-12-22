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

/**
 * StatementConstraintS3 is used to represent human knowledge about the statement is correct or not.
 * <br/>
 * 
 * S3 mean the third constraint proposed in the paper. <br/>
 * 
 * Constraint: <br/>
 * Under the situation that control dominator is correct, the statement is wrong if the read variables is correct but the written variables is wrong. <br/>
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public class StatementConstraintS3 extends StatementConstraint {

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * @throws WrongConstraintConditionException if the node does not satisfy the condition of
     *         StatementConstraintS3, which is: 1. The node has at least one predicate. 2. The node
     *         has at least one read variable and one written variable.
     * 
     */
    public StatementConstraintS3(final TraceNode node) {
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
     *         StatementConstraintS3, which is: 1. The node has at least one predicate. 2. The node
     *         has at least one read variable and one written variable.
     * 
     */
    public StatementConstraintS3(final TraceNode node, final double propagationProb) {
        super(node, genID(node), propagationProb);

        // Check that node should have at least one predicates.
        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Statement Constraint S3 without any predicates for node: "
                            + node.getOrder()));
        }

        // Check that node should have at least one read variable and one written variable.
        if (node.getReadVariables().size() == 0 || node.getWrittenVariables().size() == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Statement Constraint S3 without any read or written variables for node: "
                            + node.getOrder()));
        }
    }

    @Override
    public Factor genFactor() {
        /*
         * The invalid case is that, 1. Control dominator is correct 2. Have wrong written variable
         * 3. Have no wrong read variable 4. Have correct statement
         */


        final String factorName = this.name;

        // Collect variables. Note that the order matters.
        // Order: [read variables, written variables, control dominator, statement]
        List<Variable<?>> variables = new ArrayList<>();
        for (VarValue readVar : node.getReadVariables()) {
            variables.add(new Variable<VarValue>(readVar, 2));
        }
        for (VarValue writtenVar : node.getWrittenVariables()) {
            variables.add(new Variable<VarValue>(writtenVar, 2));
        }
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
                if (this.haveWrongWrittenVariable(indices) && !this.haveWrongReadVariable(indices)
                        && !this.haveWrongStatement(indices)) {
                    probabilityDistribution.set(1 - this.propagationProb, indices);
                }
            }
        } else {
            for (int[] indices : this.genAllPossibleIndices(numPredicates)) {
                if (this.haveWrongWrittenVariable(indices) && !this.haveWrongReadVariable(indices)
                        && !this.haveWrongControlDominator(indices)
                        && !this.haveWrongStatement(indices)) {
                    probabilityDistribution.set(1 - this.propagationProb, indices);
                }
            }
        }

        return new Factor(factorName, probabilityDistribution, variables);
    }

    private static String genID(final TraceNode node) {
        return "SS3_" + node.getOrder();
    }
}
