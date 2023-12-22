package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * PriorConstraint is used to represent prior knowledge about the target variable, such as whether
 * it is correct or not.
 */
public class PriorConstraint extends Constraint {

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
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * 
     */
    public PriorConstraint(TraceNode node, final VarValue var, double propagationProb) {
        super(node, genName(node, var), propagationProb);

        if (!node.isReadVariablesContains(var.getVarID())
                && !node.isWrittenVariablesContains(var.getVarID())) {
            throw new IllegalArgumentException("Cannot create PriorConstraint for variable "
                    + var.getVarID() + " for node " + node.getOrder());
        }

        this.var = var;
    }

    @Override
    public Factor genFactor() {
        // TODO Auto-generated method stub
        final String factorName = this.name;

        // Construct variables for factor, note that order matters
        List<Variable<?>> variables = new ArrayList<>();
        variables.add(new Variable<>(this.var, 2));

        // Calculate the probability of correctness
        final int[] shape = {2};
        HDArray array = HDArray.createBySizeWithValue(this.propagationProb, shape);
        array.set(1 - this.propagationProb, 0);

        return new Factor(factorName, array, variables);
    }

    protected static String genName(final TraceNode node, final VarValue var) {
        return "P_" + node.getOrder() + "_" + var.getVarID();
    }

}
