package microbat.debugpilot.propagation.BP.constraint;

import BeliefPropagation.graph.Factor;
import microbat.model.trace.TraceNode;

public abstract class StatementConstraint extends Constraint {

    public StatementConstraint(TraceNode node, String ID, double propagationProb) {
        super(node, ID, propagationProb);
    }

    @Override
    public abstract Factor genFactor();

    protected boolean haveWrongReadVariable(final int[] indices) {
        final int startIdx = 0;
        final int endIdx = node.getReadVariables().size();
        for (int idx = startIdx; idx < endIdx; idx++) {
            if (indices[idx] == 0) {
                return true;
            }
        }
        return false;
    }

    protected boolean haveWrongWrittenVariable(final int[] indices) {
        final int startIdx = node.getReadVariables().size();
        final int endIdx = node.getReadVariables().size() + node.getWrittenVariables().size();
        for (int idx = startIdx; idx < endIdx; idx++) {
            if (indices[idx] == 0) {
                return true;
            }
        }
        return false;
    }

    protected boolean haveWrongControlDominator(final int[] indices) {
        if (this.node.getControlDominator() == null) {
            throw new RuntimeException(
                    "Cannot check control dominator for node: " + this.node.getOrder());
        }
        final int controlDominatorIdx = indices.length - 2;
        return indices[controlDominatorIdx] == 0;
    }

    protected boolean haveWrongStatement(final int[] indices) {
        final int statementIdx = indices.length - 1;
        return indices[statementIdx] == 0;
    }

    protected int[][] genAllPossibleIndices(final int numPredicates) {
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
