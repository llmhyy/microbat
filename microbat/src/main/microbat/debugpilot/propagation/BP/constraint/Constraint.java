package microbat.debugpilot.propagation.BP.constraint;

import java.util.Objects;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.utils.Log;
import microbat.model.trace.TraceNode;

/**
 * Constraint is used to represent human knowledge about the program. It is used to generate factors
 * for the BP algorithm.
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public abstract class Constraint {

    /**
     * The name of the constraint. It is used to identify the constraint.
     */
    protected final String name;

    /**
     * The node that the constraint is associated with.
     */
    protected final TraceNode node;

    /**
     * Propagation probability.
     */
    protected final double propagationProb;

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

    /**
     * Generate a factor for the constraint.
     * 
     * @return A factor for the constraint.
     */
    public abstract Factor genFactor();

    /**
     * Get the name of this constraint
     * 
     * @return Name of this constraint
     */
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

    /**
     * Get the number of predicates in a node.
     * 
     * @param node The node to count predicates.
     * @return The number of predicates in the node.
     */
    public static int countPredicates(final TraceNode node) {
        int count = node.getReadVariables().size() + node.getWrittenVariables().size();
        if (node.getControlDominator() != null) {
            count++;
        }
        return count;
    }

    /**
     * Initialize a shape array by dimension count.
     * 
     * @param dimensionCount The dimension count.
     * @return A shape array.
     * @throws IllegalArgumentException if dimensionCount is less than 1.
     */
    protected static int[] initShapeByDimension(final int dimensionCount) {
        if (dimensionCount < 1)
            throw new IllegalArgumentException(
                    Log.genLogMsg(Constraint.class, "Dimension count must be greater than 0"));
        final int[] shape = new int[dimensionCount];
        for (int i = 0; i < dimensionCount; i++) {
            shape[i] = 2;
        }
        return shape;
    }
}
