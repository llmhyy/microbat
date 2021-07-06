package microbat.model.trace;

import java.util.List;
import java.util.function.Function;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.value.VarValue;
import sav.common.core.Pair;

public class LazyTraceNode extends TraceNode{
	private Function<TraceNode, Pair<List<VarValue>, List<VarValue>>> supplier;
	public LazyTraceNode(
			BreakPoint breakPoint, 
			BreakPointValue programState, 
			int order, 
			Trace trace, 
			Function<TraceNode, Pair<List<VarValue>, List<VarValue>>> supplier) {
		super(breakPoint, programState, order, trace);
		this.supplier = supplier;
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
		Pair<List<VarValue>, List<VarValue>> pair = supplier.apply(this);
		this.setReadVariables(pair.first());
		this.setWrittenVariables(pair.second());
	}
}
