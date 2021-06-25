package microbat.model.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;

import microbat.algorithm.graphdiff.GraphDiff;
import microbat.algorithm.graphdiff.HierarchyGraphDiffer;
import microbat.model.AttributionVar;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.Scope;
import microbat.model.UserInterestedVariables;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.util.JavaUtil;
import microbat.util.Settings;
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
