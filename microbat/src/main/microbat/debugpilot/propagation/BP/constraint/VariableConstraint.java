package microbat.debugpilot.propagation.BP.constraint;

import java.util.stream.IntStream;
import BeliefPropagation.graph.Factor;
import microbat.model.trace.TraceNode;

public abstract class VariableConstraint extends Constraint {

    public VariableConstraint(final TraceNode node, final String ID, final double propagationProb) {
        super(node, ID, propagationProb);
    }

    @Override
    public abstract Factor genFactor();

    protected int[] genInvalidIndices(final int numPredicates) {
        return IntStream
                .concat(IntStream.generate(() -> 1).limit(numPredicates - 1), IntStream.of(0))
                .toArray();

    }
}
