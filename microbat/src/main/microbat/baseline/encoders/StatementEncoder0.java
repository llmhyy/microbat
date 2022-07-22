package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.StatementConstraintA1;
import microbat.baseline.constraints.StatementConstraintA2;
import microbat.baseline.constraints.StatementConstraintA3;
import microbat.baseline.constraints.VariableConstraint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.UserFeedback;

public class StatementEncoder0 extends Encoder {
	
	public StatementEncoder0(Trace trace, List<TraceNode> executionList) {
		super(trace, executionList);
	}

	@Override
	public void encode() {
		
	}

	@Override
	protected int countPredicates(TraceNode node) {
		return super.countPredicates(node) + 1;
	}
	
	@Override
	protected boolean isSkippable(TraceNode node) {
		return node.isBranch() || this.countPredicates(node) > 30;
	}
	
	protected List<Constraint> genConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			constraints.addAll(this.genVarToStatConstraints(node));
		}
		constraints.addAll(this.genPriorConstraints());
		return constraints;
	}
	
	private List<Constraint> genVarToStatConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		if (this.isSkippable(node)) {
			return constraints;
		}
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		final int totalLen = this.countPredicates(node);
		
		TraceNode controlDom = node.getControlDominator();
		final boolean haveControlDom = controlDom != null;
		
		final int statementOrder = node.getOrder();
		final int controlDomOrder = haveControlDom ? controlDom.getOrder() : Constraint.NaN;
		
		final int writeStartIdx = readLen == 0 ? 0 : readLen;
		final int predIdx = haveControlDom ? totalLen - 2 : -1;
		final int conclusionIdx = totalLen - 1;
		
		// Constraint A1, A2, A3 include the same variable
		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
		variableIncluded.set(0, readLen + writeLen);
		
		if (haveControlDom) {
			variableIncluded.set(totalLen-2);
		}
		variableIncluded.set(conclusionIdx);
		
		// Constraint A1
		// Variable to statement constraint A1
		Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomOrder);
		constraintA1.setVarsID(node);
		
		constraints.add(constraintA1);
		
		// Variable to statement constraint A2
		Constraint constraintA2 = new StatementConstraintA2(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomOrder);
		constraints.add(constraintA2);
		
		// Variable to statement constraint A3
		Constraint constraintA3 = new StatementConstraintA3(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomOrder);
		constraints.add(constraintA3);
		
		return constraints;
	}
	
	private List<Constraint> genPriorConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		return constraints;
	}
	
	
}
