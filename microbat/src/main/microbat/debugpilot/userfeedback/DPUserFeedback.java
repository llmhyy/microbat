package microbat.debugpilot.userfeedback;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mysql.fabric.xmlrpc.base.Struct;

import microbat.log.Log;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.TraceUtil;

/**
 * User feedback for DebugPilot (DP) <br/>
 * 
 * It support multiple variable selection
 */
public class DPUserFeedback {
	
	protected final DPUserFeedbackType type;
	protected final TraceNode node;
	protected final Set<VarValue> correctVars;
	protected final Set<VarValue> wrongVars;
	
	public DPUserFeedback(final DPUserFeedbackType type, final TraceNode node) {
		this(type, node, null, null);
	}
	
	public DPUserFeedback(final DPUserFeedbackType type, final TraceNode node, final Collection<VarValue> wrongVars, Collection<VarValue> correctVars) {
		Objects.requireNonNull(type, Log.genMsg(getClass(), "Given user feedback type cannot be null"));
		Objects.requireNonNull(node, Log.genMsg(getClass(), "Given node cannot be null"));
		this.type = type;
		this.node = node;
		this.wrongVars = new HashSet<>();
		this.correctVars = new HashSet<>();
		if (wrongVars != null) {
			this.addWrongVars(wrongVars);			
		}
		if (correctVars != null) {			
			this.addCorrectVars(correctVars);
		}
	}
 
	/**
	 * Add wrong variables in set. <br/>
	 * If the given variable is already in correct set, it will be removed from correct set.
	 * @param wrongVars Wrong variables to add
	 */
	public void addWrongVars(final Collection<VarValue> wrongVars) {
		this.wrongVars.addAll(wrongVars);
		if (!this.correctVars.isEmpty()) {
			this.correctVars.removeAll(wrongVars);			
		}
	}
	
	/**
	 * Add wrong variables in set. <br/>
	 * If the given variable is already in correct set, it will be removed from correct set.
	 * @param wrongVars Wrong variables to add
	 */
	public void addWrongVar(final VarValue... wrongVars) {
		this.addWrongVars(Arrays.asList(wrongVars));
	}
	
	/**
	 * Add correct variable in set. <br/>
	 * If the give variable is already in wrong set, it will be removed from wrong set.
	 * @param correctVars Correct variables to add
	 */
	public void addCorrectVars(final Collection<VarValue> correctVars) {
		this.correctVars.addAll(correctVars);
		if (!this.wrongVars.isEmpty())
			this.wrongVars.removeAll(correctVars);
	}
	
	/**
	 * Add correct variable in set. <br/>
	 * If the give variable is already in wrong set, it will be removed from wrong set.
	 * @param correctVars Correct variables to add
	 */
	public void addCorrectVar(final VarValue... correctVars) {
		this.addCorrectVars(Arrays.asList(correctVars));
	}
	
	public DPUserFeedbackType getType() {
		return type;
	}

	public TraceNode getNode() {
		return node;
	}

	public Set<VarValue> getCorrectVars() {
		return correctVars;
	}

	public Set<VarValue> getWrongVars() {
		return wrongVars;
	}
	
	
	/**
	 * Two feedback are similar if: <br/>
	 * 1. They have the same type <br/>
	 * 2. If it is wrong variable, wrong variables set of otherFeedback should be the sub-set of this wrong variable set
	 * @param otherFeedback Feedback to compare
	 * @return True if similar
	 */
	public boolean isSimilar(final DPUserFeedback otherFeedback) {
		if (this.type != otherFeedback.type) {
			return false;
		}
		
		if (this.type == DPUserFeedbackType.WRONG_VARIABLE && !this.wrongVars.containsAll(otherFeedback.wrongVars)) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public int hashCode() {
		int hashCode = 7;
		hashCode = 17 * hashCode + this.type.hashCode();
		hashCode = 17 * hashCode + this.node.hashCode();
		hashCode = 17 * hashCode + this.wrongVars.hashCode();
		hashCode = 17 * hashCode + this.correctVars.hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (this == otherObj) return true;
		if (otherObj == null || this.getClass() != otherObj.getClass()) return false;
		
		final DPUserFeedback otherFeedback = (DPUserFeedback) otherObj;
		
		if (this.type != otherFeedback.type) {
			return false;
		}
		
		if (!this.node.equals(otherFeedback.node)) {
			return false;
		}
		
		if (!this.wrongVars.equals(otherFeedback.wrongVars)) {
			return false;
		}
		
		if (!this.correctVars.equals(otherFeedback.correctVars)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		stringBuilder.append(this.type.name() + ",");
		stringBuilder.append("Node: " + this.node.getOrder() + ",");
		stringBuilder.append("Wrong Variable: ");
		for (VarValue wrongVar : this.wrongVars) {
			stringBuilder.append(wrongVar.getVarName() + ",");
		}
		stringBuilder.append("Correct Variable: ");
		for (VarValue correctVar : this.correctVars) {
			stringBuilder.append(correctVar.getVarName() + ",");
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
}
