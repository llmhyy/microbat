package microbat.debugpilot.propagation.BP.constraint;

import BeliefPropagation.graph.Factor;
import microbat.model.trace.TraceNode;

/**
 * StatementConstraint is a abstract class for all constraint used to represent the knowledge about
 * a statement is correct or not.
 */
public abstract class StatementConstraint extends Constraint {

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param ID The name of the constraint. It is used to identify the constraint.
     * @param propagationProb Propagation probability.
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * 
     */
    public StatementConstraint(TraceNode node, String ID, double propagationProb) {
        super(node, ID, propagationProb);
    }

    @Override
    public abstract Factor genFactor();

    /**
     * Check is there any wrong read variables under given status representation, which is in the
     * form of indices.
     * 
     * @param indices The status representation.
     * @return True if there is any wrong read variables, false otherwise.
     */
    protected boolean haveWrongReadVariable(final int[] indices) {
        // The order of variable is [read variables, written variables, control dominator,
        // statement]
        final int startIdx = 0;
        final int endIdx = node.getReadVariables().size();
        for (int idx = startIdx; idx < endIdx; idx++) {
            if (indices[idx] == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check is there any wrong written variables under given status representation, which is in the
     * form of indices.
     * 
     * @param indices The status representation.
     * @return True if there is any wrong written variables, false otherwise.
     */
    protected boolean haveWrongWrittenVariable(final int[] indices) {
        // The order of variable is [read variables, written variables, control dominator,
        // statement]
        final int startIdx = node.getReadVariables().size();
        final int endIdx = node.getReadVariables().size() + node.getWrittenVariables().size();
        for (int idx = startIdx; idx < endIdx; idx++) {
            if (indices[idx] == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check is there any wrong control dominator under given status representation, which is in the
     * form of indices.
     * 
     * @param indices The status representation.
     * @return True if there is any wrong control dominator, false otherwise.
     * @throws RuntimeException if the node does not have control dominator.
     */
    protected boolean haveWrongControlDominator(final int[] indices) {
        // The order of variable is [read variables, written variables, control dominator,
        // statement]
        if (this.node.getControlDominator() == null) {
            throw new RuntimeException(
                    "Cannot check control dominator for node: " + this.node.getOrder());
        }
        final int controlDominatorIdx = indices.length - 2;
        return indices[controlDominatorIdx] == 0;
    }

    /**
     * Check is there any wrong statement under given status representation, which is in the form of
     * indices.
     * 
     * @param indices The status representation.
     * @return True if there is any wrong statement, false otherwise.
     */
    protected boolean haveWrongStatement(final int[] indices) {
        final int statementIdx = indices.length - 1;
        return indices[statementIdx] == 0;
    }

    /**
     * Generate all possible status representation for a node.
     * 
     * @param node The node to generate status representation.
     * @return All possible status representation for the node.
     */
    protected int[][] genAllPossibleIndices(final int numPredicates) {
        // All variable should only have two status: correct or wrong
        int numCombinations = (int) Math.pow(2, numPredicates);
        int[][] allPossibleIndices = new int[numCombinations][numPredicates];
        for (int i = 0; i < numCombinations; i++) {
            int[] indices = new int[numPredicates];
            for (int j = 0; j < numPredicates; j++) {
                indices[j] = (i >> j) & 1;
            }
            allPossibleIndices[i] = indices;
        }
        return allPossibleIndices;
    }

}
