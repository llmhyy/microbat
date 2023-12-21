package microbat.debugpilot.propagation.BP.constraint;

import java.util.Objects;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.utils.Log;
import microbat.model.trace.TraceNode;

public abstract class Constraint {

    protected final String name;

    protected final TraceNode node;

    protected final double propagationProb;

    public Constraint(final TraceNode node, final String ID, final double propagationProb) {
        Objects.requireNonNull(node, Log.genLogMsg(getClass(), "Node cannot be null"));
        Objects.requireNonNull(ID, Log.genLogMsg(this.getClass(), "ID cannot be null"));
        if (ID.isEmpty()) {
            throw new IllegalArgumentException(
                    Log.genLogMsg(this.getClass(), "ID cannot be empty"));
        }
        if (propagationProb < 0.0d || propagationProb > 1.0d) {
            throw new IllegalArgumentException(Log.genLogMsg(this.getClass(),
                    "Propagation probability must be between 0.0 and 1.0"));
        }
        this.node = node;
        this.name = ID;
        this.propagationProb = propagationProb;
    }

    public abstract Factor genFactor();

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int hashCode = this.node.hashCode();
        hashCode = hashCode * 31 + this.name.hashCode();
        hashCode = hashCode * 31 + Double.hashCode(this.propagationProb);
        return hashCode;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == this)
            return true;
        if (!(otherObj instanceof Constraint))
            return false;
        final Constraint otherConstraint = (Constraint) otherObj;
        return this.name.equals(otherConstraint.name) && this.node.equals(otherConstraint.node)
                && this.propagationProb == otherConstraint.propagationProb;
    }

    public static int countPredicates(final TraceNode node) {
        int count = node.getReadVariables().size() + node.getWrittenVariables().size();
        if (node.getControlDominator() != null) {
            count++;
        }
        return count;
    }

    protected static int[] initShapeByDimension(final int dimensionCount) {
        final int[] shape = new int[dimensionCount];
        for (int i = 0; i < dimensionCount; i++) {
            shape[i] = 2;
        }
        return shape;
    }
}
