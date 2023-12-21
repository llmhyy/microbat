package microbat.debugpilot.propagation.BP;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.graph.DefaultEdge;
import BeliefPropagation.alg.propagation.LoopyBeliefPropagation;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.FactorGraph;
import BeliefPropagation.graph.Variable;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.PriorConstraint;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintS1;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintS2;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintS3;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintS4;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintS5;
import microbat.debugpilot.propagation.BP.constraint.StatementConstraintS6;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementPropagator implements ProbabilityPropagator {

    protected List<TraceNode> traceNodeList;

    public StatementPropagator(List<TraceNode> traceNodeList) {
        this.traceNodeList = traceNodeList;
    }

    @Override
    public void propagate() {
        for (int idx = 0; idx < this.traceNodeList.size() - 1; idx++) {
            final TraceNode node = this.traceNodeList.get(idx);

            if (Constraint.countPredicates(node) == 0) {
                continue;
            }

            List<Constraint> constraints = new ArrayList<>();
            constraints.addAll(this.genVarToStatConstraints(node));
            constraints.addAll(this.genPriorConstraints(node));

            List<Factor> factors = constraints.stream().map(Constraint::genFactor).toList();
            final FactorGraph<DefaultEdge> factorGraph = new FactorGraph<>(DefaultEdge.class);
            for (Factor factor : factors) {
                factorGraph.addFactor(factor);
                for (Variable<?> variable : factor.getVariables()) {
                    factorGraph.addVariable(variable);
                }
            }
            factorGraph.fillEdges();

            LoopyBeliefPropagation<DefaultEdge> lbp = new LoopyBeliefPropagation<>(factorGraph);
            Variable<TraceNode> statement = new Variable<>(node, 2);
            final double correctness = lbp.getBelief(statement).getProbability().get(1);
            node.setCorrectness(correctness);
        }
    }

    protected List<Constraint> genVarToStatConstraints(final TraceNode node) {
        List<Constraint> constraints = new ArrayList<>();

        /*
         * There are three possible cases: 1. Have both read and written variables 2. Only have read
         * or written variables 3. Only have control dominator
         */

        if (!this.haveReadVar(node) && !this.haveWrittenVar(node)) {
            Constraint constraint = new StatementConstraintS6(node, PropProbability.HIGH);
            constraints.add(constraint);
        } else if (this.haveReadVar(node) && this.haveWrittenVar(node)) {
            Constraint constraintS1 = new StatementConstraintS1(node, PropProbability.HIGH);
            constraints.add(constraintS1);

            Constraint constraintS2 = new StatementConstraintS2(node, PropProbability.HIGH);
            constraints.add(constraintS2);

            Constraint constraintS3 = new StatementConstraintS3(node, PropProbability.HIGH);
            constraints.add(constraintS3);
        } else {

            for (VarValue readVar : node.getReadVariables()) {
                Constraint constrainS4 =
                        new StatementConstraintS4(node, readVar, PropProbability.HIGH);
                constraints.add(constrainS4);
                Constraint constraintS5 =
                        new StatementConstraintS5(node, readVar, PropProbability.HIGH);
                constraints.add(constraintS5);
            }

            for (VarValue writeVar : node.getWrittenVariables()) {
                Constraint constrainS4 =
                        new StatementConstraintS4(node, writeVar, PropProbability.HIGH);
                constraints.add(constrainS4);
                Constraint constraintS5 =
                        new StatementConstraintS5(node, writeVar, PropProbability.HIGH);
                constraints.add(constraintS5);
            }
        }

        return constraints;
    }

    protected boolean haveReadVar(final TraceNode node) {
        return !node.getReadVariables().isEmpty();
    }

    protected boolean haveWrittenVar(final TraceNode node) {
        return !node.getWrittenVariables().isEmpty();
    }

    protected boolean haveControlDom(final TraceNode node) {
        return node.getControlDominator() != null;
    }

    protected List<Constraint> genPriorConstraints(final TraceNode node) {
        List<Constraint> constraints = new ArrayList<>();

        for (VarValue readVar : node.getReadVariables()) {
            Constraint constraint = new PriorConstraint(node, readVar, readVar.getCorrectness());
            constraints.add(constraint);
        }

        for (VarValue writtenVar : node.getWrittenVariables()) {
            Constraint constraint =
                    new PriorConstraint(node, writtenVar, writtenVar.getCorrectness());
            constraints.add(constraint);
        }

        return constraints;
    }



}
