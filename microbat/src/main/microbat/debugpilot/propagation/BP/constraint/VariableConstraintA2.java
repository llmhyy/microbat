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

public class VariableConstraintA2 extends VariableConstraint {

    protected final VarValue targetReadVar;

    public VariableConstraintA2(final TraceNode node, final VarValue targetReadVar) {
        this(node, targetReadVar, PropProbability.HIGH);
    }

    public VariableConstraintA2(final TraceNode node, final VarValue targetReadVar,
            final double propagationProb) {
        super(node, VariableConstraintA2.genID(node), propagationProb);

        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Variable Constraint A2 without any predicates for node: "
                            + node.getOrder()));
        }

        if (!node.isReadVariablesContains(targetReadVar.getVarID())) {
            throw new WrongConstraintConditionException(
                    Log.genMsg(getClass(), "Conclusion variable; " + targetReadVar.getVarID()
                            + " does not belongs to this node: " + node.getOrder()));
        }

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
        List<Variable<?>> variables = new ArrayList<>();
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
