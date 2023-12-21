package microbat.debugpilot.propagation.BP.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.HDArray;
import BeliefPropagation.graph.Variable;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.log.Log;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementConstraintS1 extends StatementConstraint {

    public StatementConstraintS1(final TraceNode node) {
        this(node, PropProbability.HIGH);
    }

    public StatementConstraintS1(final TraceNode node, final double propagationProb) {
        super(node, genID(node), propagationProb);

        if (Constraint.countPredicates(node) == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Statement Constraint S1 without any predicates for node: "
                            + node.getOrder()));
        }

        if (node.getReadVariables().size() == 0 || node.getWrittenVariables().size() == 0) {
            throw new WrongConstraintConditionException(Log.genMsg(getClass(),
                    "Cannot form Statement Constraint S1 without any read or written variables for node: "
                            + node.getOrder()));
        }
    }

    @Override
    public Factor genFactor() {
        // The only invalid case is when all the predicates are correct,
        // the statement is incorrect.

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
        final int[] invalidIndices = IntStream
                .concat(IntStream.generate(() -> 1).limit(numPredicates - 1), IntStream.of(0))
                .toArray();
        probabilityDistribution.set(1 - this.propagationProb, invalidIndices);

        return new Factor(factorName, probabilityDistribution, variables);
    }

    private static String genID(final TraceNode node) {
        return "SS1_" + node.getOrder();
    }

}
