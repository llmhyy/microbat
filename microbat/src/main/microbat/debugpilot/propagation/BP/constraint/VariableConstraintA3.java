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
 * VariableConstraintA3 is used to represent human knowledge about the variable is correct or not.
 * <br/>
 * 
 * A3 mean the third constraint proposed in the paper. <br/>
 * 
 * Constraint: <br/>
 * If all the read and written variable are correct, then the control dominator should also be
 * correct. <br/>
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public class VariableConstraintA3 extends VariableConstraint {

    /**
     * The variable that the constraint is associated with.
     */
    protected final VarValue controlDom;

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @see VariableConstraintA3#VariableConstraintA3(TraceNode, double)
     */
    public VariableConstraintA3(final TraceNode node) {
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
     *         VariableConstraintA3, which is: 1. The node has at least one predicate. 2. The node
     *         has at least one read variable and one written variable. 3. The node should have
     *         control dominator
     * 
     */
    public VariableConstraintA3(final TraceNode node, final double propagtionProbability) {
        super(node, VariableConstraintA3.genID(node), propagtionProbability);

        // Check that node should have at least one predicates
        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A3 without any predicates for node: "
                            + node.getOrder()));
        }

        // Check that node should have control dominator
        if (node.getControlDominator() == null) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A3 without control dominator for node: "
                            + node.getOrder()));
        }

        // Check that node should have at least one read variable and one written variable
        if (Constraint.countPredicates(node) == 1) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A3 without any written variables and read variables for node: "
                            + node.getOrder()));
        }

        this.controlDom = node.getControlDominator().getConditionResult();
    }

    @Override
    public Factor genFactor() {
        // For VA3, the invalid case is that all the other written variables and read variables
        // are correct, but the control dominator is wrong
        final String factorName = this.name;

        // Construct variables for factor, note that order matters
        // Order: [read variables, written variables, control dominator]
        List<Variable<?>> variables = new ArrayList<>();
        for (VarValue varValue : node.getReadVariables()) {
            variables.add(new Variable<>(varValue, 2));
        }
        for (VarValue varValue : node.getWrittenVariables()) {
            variables.add(new Variable<>(varValue, 2));
        }
        variables.add(new Variable<>(this.controlDom, 2));

        // Calculate probability distribution
        final int numPredicates = variables.size();
        final int[] shape = Constraint.initShapeByDimension(numPredicates);
        HDArray probabilityDistribution =
                HDArray.createBySizeWithValue(this.propagationProb, shape);
        final int[] invalidIndices = this.genInvalidIndices(numPredicates);
        probabilityDistribution.set(1.0d - this.propagationProb, invalidIndices);

        return new Factor(factorName, probabilityDistribution, variables);
    }

    private static String genID(final TraceNode node) {
        return "VA3_" + node.getOrder();
    }

}
