package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import BeliefPropagation.utils.Log;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * VariableConstraintA1 is used to represent human knowledge about the conclusion variable is
 * correct or not. <br/>
 * 
 * A1 mean the first constraint proposed in the paper. <br/>
 * 
 * Constraint: <br/>
 * Under the situation that control dominator is correct, the written variable is correct if all
 * the other read variables and control dominator are correct. <br/>
 * 
 * Please check the following paper for more details: Z. Xu, S. Ma, X. Zhang, S. Zhu and B. Xu,
 * "Debugging with Intelligence via Probabilistic Inference," 2018 IEEE/ACM 40th International
 * Conference on Software Engineering (ICSE), Gothenburg, Sweden, 2018, pp. 1171-1181, doi:
 * 10.1145/3180155.3180237.
 * 
 * @see Factor
 */
public class VariableConstraintA1 extends VariableConstraint {

    /**
     * The variable that the constraint is associated with.
     */
    protected final VarValue targetWrittenVar;

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param targetWrittenVar The variable that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @see VariableConstraintA1#VariableConstraintA1(TraceNode, VarValue, double)
     */
    public VariableConstraintA1(final TraceNode node, final VarValue targetWrittenVar) {
        this(node, targetWrittenVar, PropProbability.HIGH);
    }

    /**
     * Constructor.
     * 
     * @param node The node that the constraint is associated with.
     * @param targetWrittenVar The variable that the constraint is associated with.
     * @param propagationProb Propagation probability.
     * @throws NullPointerException if node or ID is null.
     * @throws IllegalArgumentException if ID is empty or propagationProb is not between 0.0 and
     *         1.0.
     * @throws WrongConstraintConditionException if the node does not satisfy the condition of
     *         VariableConstraintA1, which is: 1. The node has at least one predicate. 2. The node
     *         has at least one read variable and one written variable. 3. The conclusion variable
     *         must be a written variable of the node.
     * 
     */
    public VariableConstraintA1(final TraceNode node, final VarValue targetWrittenVar,
            final double propagationProb) {
        super(node, VariableConstraintA1.genID(node), propagationProb);

        Objects.requireNonNull(targetWrittenVar,
                Log.genLogMsg(getClass(), "Null target written variable"));

        // Cannot form constraint if there is no predicate
        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genLogMsg(getClass(),
                    "Cannot form Variable Constraint A1 without any predicates for node: "
                            + node.getOrder()));
        }

        // Given written variable must be a written variable of the node
        if (!node.isWrittenVariablesContains(targetWrittenVar.getVarID())) {
            throw new WrongConstraintConditionException(
                    Log.genLogMsg(getClass(), "Conclusion variable; " + targetWrittenVar.getVarID()
                            + " does not belongs to this node: " + node.getOrder()));
        }

        // Cannot form constraint if there are not read variables and control dominator
        if (Constraint.countPredicates(node) == node.getWrittenVariables().size()) {
            throw new WrongConstraintConditionException(Log.genLogMsg(getClass(),
                    "Cannot form Variable Constraint A1 without any read variables and control dominator for node: "
                            + node.getOrder()));
        }

        this.targetWrittenVar = targetWrittenVar;
    }

    @Override
    public Factor genFactor() {
        // For VA1, the only invalid case is that all the other read variables and control dominator
        // are correct, but the conclusion variable is wrong
        final int numPredicates =
                node.getControlDominator() == null ? node.getReadVariables().size() + 1
                        : node.getReadVariables().size() + 2;
        final int[] shape = Constraint.initShapeByDimension(numPredicates);

        // Construct variables for factor, note that order matters
        // Order: [read variables, control dominator, conclusion variable]
        final List<Variable<?>> variables = new ArrayList<>();
        for (VarValue var : node.getReadVariables()) {
            variables.add(new Variable<VarValue>(var, 2));
        }
        if (node.getControlDominator() != null) {
            final VarValue conditionResult = node.getControlDominator().getConditionResult();
            variables.add(new Variable<VarValue>(conditionResult, 2));
        }
        variables.add(new Variable<VarValue>(targetWrittenVar, 2));

        // Calculate probability distribution
        HDArray probDistribution = HDArray.createBySizeWithValue(this.propagationProb, shape);
        final int[] invalidIndices = this.genInvalidIndices(numPredicates);
        probDistribution.set(1 - this.propagationProb, invalidIndices);

        // Constructor factor
        final String factorName = this.name;
        return new Factor(factorName, probDistribution, variables);
    }

    protected static String genID(final TraceNode node) {
        return "VA1_" + node.getOrder();
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == this)
            return true;
        if (!(otherObj instanceof VariableConstraintA1))
            return false;
        if (!super.equals(otherObj)) {
            return false;
        }
        final VariableConstraintA1 otherConstraint = (VariableConstraintA1) otherObj;
        return this.targetWrittenVar.equals(otherConstraint.targetWrittenVar);
    }

}
