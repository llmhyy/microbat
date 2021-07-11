package microbat.model.trace;

import java.util.List;
import java.util.function.Function;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.value.VarValue;
import microbat.sql.LazySupplier;
import sav.common.core.Pair;

public class LazyTraceNode extends TraceNode {
	private final LazySupplier supplier;
	private List<TraceNode> abstractChildren = null;
	private boolean isParent;

	public LazyTraceNode(BreakPoint breakPoint, BreakPointValue programState, int order, Trace trace,
			LazySupplier supplier) {
		super(breakPoint, programState, order, trace);
		this.supplier = supplier;
	}

	@Override
	public List<TraceNode> getAbstractChildren() {
		if (this.abstractChildren == null) {
			List<TraceNode> invocationChildren = this.supplier.loadInvocationChildren(this);
			invocationChildren.forEach(child -> child.setInvocationParent(this));
			this.setInvocationChildren(invocationChildren);
			List<TraceNode> loopChildren = this.supplier.loadLoopChildren(this);
			loopChildren.forEach(child -> child.setLoopParent(this));
			this.setLoopChildren(loopChildren);
			this.abstractChildren = super.getAbstractChildren();

			// insert new children into existing trace
			// FIXME not the best way (too many side effects)
			this.getTrace().addAllTraceNode(this.abstractChildren);
		}
		return this.abstractChildren;
	}

	@Override
	public List<VarValue> getReadVariables() {
		if (this.readVariables.isEmpty()) {
			this.populateRWVars();
		}
		return readVariables;
	}

	@Override
	public List<VarValue> getWrittenVariables() {
		if (this.writtenVariables.isEmpty()) {
			this.populateRWVars();
		}
		return writtenVariables;
	}

	private void populateRWVars() {
		Pair<List<VarValue>, List<VarValue>> pair = this.supplier.loadRWVars(this);
		this.setReadVariables(pair.first());
		this.setWrittenVariables(pair.second());
	}
}
