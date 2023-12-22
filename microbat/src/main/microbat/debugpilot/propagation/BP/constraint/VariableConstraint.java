package microbat.debugpilot.propagation.BP.constraint;

import java.util.stream.IntStream;
import BeliefPropagation.graph.Factor;
import microbat.model.trace.TraceNode;

/**
 * VariableConstraint is a abstract class for all constraint used to represent the knowledge about a
 * variable is correct or not.
 */
public abstract class VariableConstraint extends Constraint {

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param ID The name of the constraint. It is used to identify the constraint.
     * @param propagationProb Propagation probability.
     * @see Constraint#Constraint(TraceNode, String, double)
     * 
     */
    public VariableConstraint(final TraceNode node, final String ID, final double propagationProb) {
        super(node, ID, propagationProb);
    }

    @Override
    public abstract Factor genFactor();

    /**
     * Generate the indices that represent the status of the variables, which violate the proposed
     * constraint.
     * 
     * @param numPredicates The number of predicates.
     * @return The indices that represent the status of the variables.
     */
    protected int[] genInvalidIndices(final int numPredicates) {
        return IntStream
                .concat(IntStream.generate(() -> 1).limit(numPredicates - 1), IntStream.of(0))
                .toArray();

    }
}
