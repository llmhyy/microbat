package microbat.debugpilot.propagation.BP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import BeliefPropagation.utils.Log;
import microbat.debugpilot.propagation.ProbabilityPropagator;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class ProbInfer implements ProbabilityPropagator {

    protected List<TraceNode> trace;

    protected Set<VarValue> wrongVars;
    protected Set<VarValue> correctVars;

    protected Set<DPUserFeedback> feedbackRecords;

    public ProbInfer(final Collection<TraceNode> trace, final Collection<VarValue> wrongVars,
            final Collection<VarValue> correctVars,
            final Collection<DPUserFeedback> feedbackRecords) {
        Objects.requireNonNull(trace, Log.genLogMsg(getClass(), "Trace should not be null"));
        Objects.requireNonNull(wrongVars,
                Log.genLogMsg(getClass(), "Wrong variables should not be null"));
        Objects.requireNonNull(correctVars,
                Log.genLogMsg(getClass(), "Correct variables should not be null"));
        Objects.requireNonNull(feedbackRecords,
                Log.genLogMsg(getClass(), "Feedbacks should not be null"));
        this.trace = new ArrayList<>(trace);
        this.wrongVars = new HashSet<>(wrongVars);
        this.correctVars = new HashSet<>(correctVars);
        this.feedbackRecords = new HashSet<>(feedbackRecords);
    }

    public ProbInfer(final PropagatorSettings settings) {
        this(settings.getSlicedTrace(), settings.getWrongVars(), settings.getCorrectVars(),
                settings.getFeedbacks());
    }

    @Override
    public void propagate() {
        VariablePropagator varPropagator = new VariablePropagator(this.trace, this.correctVars,
                this.wrongVars, this.feedbackRecords);
        varPropagator.propagate();

        StatementPropagator statementPropagator = new StatementPropagator(this.trace);
        statementPropagator.propagate();
    }

}
