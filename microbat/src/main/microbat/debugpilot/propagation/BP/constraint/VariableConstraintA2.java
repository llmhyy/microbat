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
 * VariableConstraintA2 is used to represent human knowledge about the variable is correct or not.
 * <br/>
 * 
 * A2 mean the second constraint proposed in the paper. <br/>
 * 
 * Constraint: <br/>
 * Under the situation that control dominator is correct, the target read variable is correct when
 * all other read variables, written variables, and control dominator are correct. <br/>
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public class VariableConstraintA2 extends VariableConstraint {

    /**
     * The variable that the constraint is associated with.
     */
    protected final VarValue targetReadVar;

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param targetReadVar The variable that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @see VariableConstraintA2#VariableConstraintA2(TraceNode, VarValue, double)
     */
    public VariableConstraintA2(final TraceNode node, final VarValue targetReadVar) {
        this(node, targetReadVar, PropProbability.HIGH);
    }

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param targetReadVar The variable that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * @throws WrongConstraintConditionException if the node does not satisfy the condition of
     *         VariableConstraintA2, which is: 1. The node has at least one predicate. 2. The node
     *         contain the target read variable. 3. The node has at least one written variable and
     *         one control dominator.
     * 
     */
    public VariableConstraintA2(final TraceNode node, final VarValue targetReadVar,
            final double propagationProb) {
        super(node, VariableConstraintA2.genID(node), propagationProb);

        // Check that node should have at least one predicates.
        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A2 without any predicates for node: "
                            + node.getOrder()));
        }

        // Check that node should contain the target read variable.
        if (!node.isReadVariablesContains(targetReadVar.getVarID())) {
            throw new WrongConstraintConditionException(
                    Log.genMsg(getClass(), "Conclusion variable; " + targetReadVar.getVarID()
                            + " does not belongs to this node: " + node.getOrder()));
        }

        // Check that the node should have other written variables or control dominator.
        if (Constraint.countPredicates(node) == node.getReadVariables().size()) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A2 without any written variables and control dominator for node: "
                            + node.getOrder()));
        }

        this.targetReadVar = targetReadVar;
    }

    private static String genID(final TraceNode node) {
        return "VA2_" + node.getOrder();
    }

    @Override
    public Factor genFactor() {
        // For VA2, the invalid case is that all the other written variables and control dominator
        // are correct, but the conclusion variable is wrong
        final String factorName = this.name;

        // Construct variables for factor, note that order matters
        // Order: [other read variables, written variables, control dominator, target read variable]
        List<Variable<?>> variables = new ArrayList<>();
        for (VarValue readVar : node.getReadVariables()) {
            if (!readVar.equals(targetReadVar)) {
                variables.add(new Variable<>(readVar, 2));
            }
        }
        for (VarValue varValue : node.getWrittenVariables()) {
            variables.add(new Variable<>(varValue, 2));
        }
        if (node.getControlDominator() != null) {
            final VarValue controlDomVar = node.getControlDominator().getConditionResult();
            variables.add(new Variable<>(controlDomVar, 2));
        }
        variables.add(new Variable<>(targetReadVar, 2));

        // Calculate probability distribution
        final int numPredicates = variables.size();
        final int[] shape = Constraint.initShapeByDimension(numPredicates);
        HDArray probabilityDistribution =
                HDArray.createBySizeWithValue(this.propagationProb, shape);
        final int[] invalidIndices = this.genInvalidIndices(numPredicates);
        probabilityDistribution.set(1 - this.propagationProb, invalidIndices);

        return new Factor(factorName, probabilityDistribution, variables);
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == this)
            return true;
        if (!(otherObj instanceof VariableConstraintA2))
            return false;
        final VariableConstraintA2 otherConstraint = (VariableConstraintA2) otherObj;
        return super.equals(otherObj) && this.targetReadVar.equals(otherConstraint.targetReadVar);
    }
}
