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

public class StatementConstraintS3 extends StatementConstraint {

    public StatementConstraintS3(final TraceNode node) {
        this(node, PropProbability.HIGH);
    }

    public StatementConstraintS3(final TraceNode node, final double propagationProb) {
        super(node, genID(node), propagationProb);

        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Statement Constraint S3 without any predicates for node: "
                            + node.getOrder()));
        }

        if (node.getReadVariables().size() == 0 || node.getWrittenVariables().size() == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Statement Constraint S3 without any read or written variables for node: "
                            + node.getOrder()));
        }
    }

    @Override
    public Factor genFactor() {
        /*
         * The invalid case is that, 1. Control dominator is correct 2. Have wrong written variable
         * 3. Have no wrong read variable 4. Have correct statement
         */


        final String factorName = this.name;

        // Collect variables. Note that the order matters.
        List<Variable<?>> variables = new ArrayList<>();
        for (VarValue readVar : node.getReadVariables()) {
            variables.add(new Variable<VarValue>(readVar, 2));
        }
        for (VarValue writtenVar : node.getWrittenVariables()) {
            variables.add(new Variable<VarValue>(writtenVar, 2));
        }
        if (this.node.getControlDominator() != null) {
            variables.add(new Variable<VarValue>(
                    this.node.getControlDominator().getConditionResult(), 2));
        }
        variables.add(new Variable<TraceNode>(this.node, 2));

        // Calculate probability distribution
        final int numPredicates = variables.size();
        final int[] shape = Constraint.initShapeByDimension(numPredicates);
        HDArray probabilityDistribution =
                HDArray.createBySizeWithValue(this.propagationProb, shape);
        if (node.getControlDominator() == null) {
            for (int[] indices : this.genAllPossibleIndices(numPredicates)) {
                if (this.haveWrongWrittenVariable(indices) && !this.haveWrongReadVariable(indices)
                        && !this.haveWrongStatement(indices)) {
                    probabilityDistribution.set(1 - this.propagationProb, indices);
                }
            }
        } else {
            for (int[] indices : this.genAllPossibleIndices(numPredicates)) {
                if (this.haveWrongWrittenVariable(indices) && !this.haveWrongReadVariable(indices)
                        && !this.haveWrongControlDominator(indices)
                        && !this.haveWrongStatement(indices)) {
                    probabilityDistribution.set(1 - this.propagationProb, indices);
                }
            }
        }

        return new Factor(factorName, probabilityDistribution, variables);
    }

    private static String genID(final TraceNode node) {
        return "SS3_" + node.getOrder();
    }
}
