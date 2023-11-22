package microbat.concurrent.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.UserInterestedVariables;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.JavaUtil;
import microbat.views.ReferenceAnnotation;

/**
 * Represents a concurrent trace node
 * 
 * Adds additional logic for determining dependencies
 * @author Gabau
 *
 */
public class ConcurrentTraceNode extends TraceNode {
	
	
	/**
	 * The index of the trace that this trace node is in
	 */
	protected int currentTraceId = 0;
	protected boolean isAtomic = false;
	protected TraceNode initialTraceNode;
	protected int concurrentOrder;
	/**
	 * The thread id
	 */
	protected long threadId = 0;
	
	public long getCurrentThread() {
		return initialTraceNode.getTrace().getThreadId();
	}
	

	@Override
	public int getSequentialOrder() {
		return super.getOrder();
	}
	
	@Override
	public int getOrder() {
		return concurrentOrder;
	}
	
	@Override
	public void setOrder(int concurrentOrder) {
		this.concurrentOrder = concurrentOrder;
	}
	
	
	/**
	 * Pointer to the trace node which writes to this trace node / reads to this trace node
	 * nullable
	 */
	private ConcurrentTraceNode linkedTraceNode;
	public int getCurrentTraceId() {
		return currentTraceId;
	}

	/** 
	 * Link the read and write trace nodes
	 * @return
	 */
	public ConcurrentTraceNode getLinkedTraceNode() {
		return linkedTraceNode;
	}
	
	
	public ConcurrentTraceNode(BreakPoint breakPoint, BreakPointValue programState, int order, Trace trace,
			String bytecode, long threadId) {
		super(breakPoint, programState, order, trace, bytecode);
		this.threadId = threadId;
		// TODO Auto-generated constructor stub
	}
	
	public ConcurrentTraceNode(TraceNode node, int currentTraceId, long threadId) {
		super(node.getBreakPoint(), node.getProgramState(), 
				node.getOrder(), node.getTrace(), node.getBytecode());
		this.setTimestamp(node.getTimestamp());
		this.currentTraceId = currentTraceId;
		this.threadId = threadId;
		if (node.getControlDominator() != null) {
			assert(node.getControlDominator().getLinkedConcurrentNode().isPresent());
			this.setControlDominator(node.getControlDominator().getLinkedConcurrentNode().get());
		}
	}
	
	/**
	 * Due to the way concurrency works, we separate the read and the
	 * writes, unless there is a synchronisation step.
	 * @return
	 */
	public boolean isWrite() {
		return this.writtenVariables.size() > 0;
	}
	
	public boolean isAtomic() {
		return isAtomic;
	}


	/**
	 * This implementation currently means that if this
	 * node only has write, we get the written VarValues that match the selection.
	 * 
	 */
	@Override
	public List<VarValue> getWrongReadVars(UserInterestedVariables interestedVariables) {
		if (this.isAtomic()) {
			return super.getWrongReadVars(interestedVariables);
		}
		if (this.isWrite()) {
			
			List<VarValue> vars = new ArrayList<>();
			for(VarValue var: getWrittenVariables()){
				if(interestedVariables.contains(var)){
					vars.add(var);
				}
				List<VarValue> children = var.getAllDescedentChildren();
				for(VarValue child: children){
					if(interestedVariables.contains(child)){
						if(!vars.contains(child)){
							vars.add(child);
						}
					}
				}
			}
			
			return vars;
		}
		return super.getWrongReadVars(interestedVariables);
	}

	public TraceNode getInitialTraceNode() {
		return this.initialTraceNode;
	}
	
	/**
	 * Split a given trace node into read and write concurrent
	 * @param node
	 * @return
	 */
	public static List<ConcurrentTraceNode> splitTraceNode(TraceNode node, int traceId, long threadId) {
		ArrayList<ConcurrentTraceNode> result = new ArrayList<>();
		ConcurrentTraceNode r = new ConcurrentTraceNode(node, traceId, threadId);
		r.setReadVariables(node.getReadVariables());
		r.setWrittenVariables(node.getWrittenVariables());
		node.setConcurrentTraceNode(r);
		r.initialTraceNode = node;
		r.isAtomic = true;
		return List.of(r);
		
//		if (node.getReadVariables().size() == 0 && node.getWrittenVariables().size() == 0) {
//			ConcurrentTraceNode r = new ConcurrentTraceNode(node, threadId);
//			node.setConcurrentTraceNode(r);
//			return List.of(r);
//		}
//		if (node.getReadVariables().size() > 0) {
//			ConcurrentTraceNode readVariables = new ConcurrentTraceNode(node, threadId);
//			readVariables.setReadVariables(node.getReadVariables());
//			node.setConcurrentTraceNode(readVariables);
//			result.add(readVariables);
//		}
//		if (node.getWrittenVariables().size() > 0) {
//			ConcurrentTraceNode writeVariables = new ConcurrentTraceNode(node, threadId);
//			writeVariables.setWrittenVariables(node.getWrittenVariables());
//			result.add(writeVariables);
//			if (result.size() == 2) {
//				result.get(1).linkedTraceNode = result.get(0);
//				result.get(0).linkedTraceNode = result.get(1);
//			}
//		}
//		return result;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Current threadId: " + this.currentTraceId + " " + super.toString();
	}
	
	
	
	
	
}
