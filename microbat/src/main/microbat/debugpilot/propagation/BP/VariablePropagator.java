package microbat.debugpilot.propagation.BP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import BeliefPropagation.alg.propagation.LoopyBeliefPropagation;
import BeliefPropagation.graph.Factor;
import BeliefPropagation.graph.FactorGraph;
import BeliefPropagation.graph.Variable;
import BeliefPropagation.utils.Log;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.propagation.BP.constraint.Constraint;
import microbat.debugpilot.propagation.BP.constraint.PriorConstraint;
import microbat.debugpilot.propagation.BP.constraint.VariableConstraintA1;
import microbat.debugpilot.propagation.BP.constraint.VariableConstraintA2;
import microbat.debugpilot.propagation.BP.constraint.VariableConstraintA3;
import microbat.debugpilot.propagation.probability.PropProbability;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * This class is the main class for belief propagation. It propagates the probability of correctness
 * of variables.
 */
public class VariablePropagator implements ProbabilityPropagator {

    /**
     * The set of correct variables.
     */
    protected Set<VarValue> correctVars;

    /**
     * The trace to be propagated.
     */
    protected List<TraceNode> traceNodeList;

    /**
     * The set of wrong variables.
     */
    protected Set<VarValue> wrongVars;

    /**
     * Constructs a {@code ProbInfer} with the given trace, wrong variables, correct variables and
     * feedbacks.
     *
     * @param traceNodeList the trace to be propagated
     * @param wrongVars the set of wrong variables
     * @param correctVars the set of correct variables
     * @param feedbackRecords the set of feedbacks
     * @throws NullPointerException if the trace, wrong variables, correct variables or feedbacks is
     *         {@code null}
     */
    public VariablePropagator(List<TraceNode> traceNodeList, Collection<VarValue> correctVars,
            Collection<VarValue> wrongVars, Collection<DPUserFeedback> feedbackRecords) {
        Objects.requireNonNull(traceNodeList,
                Log.genLogMsg(getClass(), "Trace should not be null"));
        Objects.requireNonNull(correctVars,
                Log.genLogMsg(getClass(), "Correct variables should not be null"));
        Objects.requireNonNull(wrongVars,
                Log.genLogMsg(getClass(), "Wrong variables should not be null"));

        this.traceNodeList = traceNodeList;
        this.correctVars = new HashSet<>(correctVars);
        this.wrongVars = new HashSet<>(wrongVars);
        this.fuseUserFeedback(feedbackRecords);
    }

    /**
     * Update the correct and wrong variables according to the feedbacks.
     * 
     * @param feedbackRecords
     */
    protected void fuseUserFeedback(final Collection<DPUserFeedback> feedbackRecords) {
        for (DPUserFeedback feedback : feedbackRecords) {
            final TraceNode node = feedback.getNode();
            final TraceNode controlDom = node.getControlDominator();

            switch (feedback.getType()) {
                case CORRECT:
                    this.correctVars.addAll(node.getReadVariables());
                    this.correctVars.addAll(node.getWrittenVariables());
                    if (controlDom != null) {
                        this.correctVars.add(controlDom.getConditionResult());
                    }
                    break;
                case ROOT_CAUSE:
                    break;
                case WRONG_PATH:
                    if (controlDom == null) {
                        throw new RuntimeException(
                                Log.genLogMsg(getClass(), "There are no control dominator"));
                    }
                    final VarValue controlDomVar = controlDom.getConditionResult();
                    this.wrongVars.add(controlDomVar);
                    break;
                case WRONG_VARIABLE:
                    this.wrongVars.addAll(feedback.getWrongVars());
                    this.correctVars.addAll(feedback.getCorrectVars());
                    this.wrongVars.addAll(node.getWrittenVariables());
                    if (controlDom != null) {
                        this.correctVars.add(controlDom.getConditionResult());
                    }
                    break;
                default:
                    throw new RuntimeException(Log.genLogMsg(getClass(), "Unknown feedback type"));
            }
        }
    }

    /**
     * Generates the constraints.
     * 
     * @return the constraints
     */
    protected List<Constraint> generateConstraints() {
        List<Constraint> constraints = new ArrayList<>();
        for (TraceNode node : this.traceNodeList) {
            constraints.addAll(this.genVarConstraints(node));
        }
        constraints.addAll(this.genPriorConstraints());
        return constraints;
    }

    /**
     * Generate variable constraints
     * 
     * @param node the node to be processed
     * @return the constraints
     */
    protected List<Constraint> genVarConstraints(final TraceNode node) {
        List<Constraint> constraints = new ArrayList<>();

        if (Constraint.countPredicates(node) == 0) {
            return constraints;
        }

        if (this.haveReadVar(node) || this.haveControlDom(node)) {
            for (VarValue writtenVar : node.getWrittenVariables()) {
                Constraint constraint = new VariableConstraintA1(node, writtenVar);
                constraints.add(constraint);
            }
        }

        if (this.haveWrittenVar(node) || this.haveControlDom(node)) {
            for (VarValue readVar : node.getReadVariables()) {
                Constraint constraint = new VariableConstraintA2(node, readVar);
                constraints.add(constraint);
            }
        }

        if (this.haveControlDom(node) && (this.haveReadVar(node) || this.haveWrittenVar(node))) {
            Constraint constraint = new VariableConstraintA3(node);
            constraints.add(constraint);
        }

        return constraints;
    }

    /**
     * Checks whether the given node has read variables.
     * 
     * @param node the node to be checked
     * @return {@code true} if the node has read variables; {@code false} otherwise
     */
    protected boolean haveReadVar(final TraceNode node) {
        return !node.getReadVariables().isEmpty();
    }

    /**
     * Checks whether the given node has written variables.
     * 
     * @param node the node to be checked
     * @return {@code true} if the node has written variables; {@code false} otherwise
     */
    protected boolean haveWrittenVar(final TraceNode node) {
        return !node.getWrittenVariables().isEmpty();
    }

    /**
     * Checks whether the given node has control dominator.
     * 
     * @param node the node to be checked
     * @return {@code true} if the node has control dominator; {@code false} otherwise
     */
    protected boolean haveControlDom(final TraceNode node) {
        return node.getControlDominator() != null;
    }

    /**
     * Generates the prior constraints.
     * 
     * @return the constraints
     */
    protected List<Constraint> genPriorConstraints() {
        List<Constraint> constraints = new ArrayList<>();

        for (TraceNode node : this.traceNodeList) {
            List<VarValue> variables = new ArrayList<>();
            variables.addAll(node.getReadVariables());
            variables.addAll(node.getWrittenVariables());
            for (VarValue var : variables) {
                if (this.correctVars.contains(var)) {
                    Constraint constraint = new PriorConstraint(node, var, PropProbability.HIGH);
                    constraints.add(constraint);
                } else if (this.wrongVars.contains(var)) {
                    Constraint constraint = new PriorConstraint(node, var, PropProbability.LOW);
                    constraints.add(constraint);
                }
            }
        }

        return constraints;
    }

    @Override
    public void propagate() {
        // Generate constraints
        Collection<Constraint> constraints = generateConstraints();

        // Constructor factor graph from constraints
        List<Factor> factors = constraints.stream().map(Constraint::genFactor).toList();
        final FactorGraph<DefaultEdge> factorGraph = new FactorGraph<>(DefaultEdge.class);
        for (Factor factor : factors) {
            factorGraph.addFactor(factor);
            for (Variable<?> variable : factor.getVariables()) {
                factorGraph.addVariable(variable);
            }
        }
        factorGraph.fillEdges();

        // Run belief propagation
        LoopyBeliefPropagation<DefaultEdge> lbp = new LoopyBeliefPropagation<>(factorGraph);
        List<Variable<?>> variables = factors.stream()
                .flatMap(factor -> factor.getVariables().stream()).distinct().toList();

        // Set belief of correctness of each variable
        for (Variable<?> variable : variables) {
            VarValue varValue = (VarValue) variable.getData();
            final double correctness = lbp.getBelief(variable).getProbability().get(1);
            varValue.setCorrectness(correctness);
        }
    }
}
