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
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
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
	
	
	protected int currentTraceId = 0;
	
	public int getCurrentTraceId() {
		return currentTraceId;
	}

	public ConcurrentTraceNode(BreakPoint breakPoint, BreakPointValue programState, int order, Trace trace,
			String bytecode) {
		super(breakPoint, programState, order, trace, bytecode);
		// TODO Auto-generated constructor stub
	}
	
	public ConcurrentTraceNode(TraceNode node, int currentTraceId) {
		super(node.getBreakPoint(), node.getProgramState(), 
				node.getOrder(), node.getTrace(), node.getBytecode());
		this.setTimestamp(node.getTimestamp());
		this.currentTraceId = currentTraceId;
	}



	
	/**
	 * Split a given trace node into read and write concurrent
	 * @param node
	 * @return
	 */
	public static List<ConcurrentTraceNode> splitTraceNode(TraceNode node, int threadId) {
		ArrayList<ConcurrentTraceNode> result = new ArrayList<>();
		if (node.getReadVariables().size() == 0 && node.getWrittenVariables().size() == 0) {
			ConcurrentTraceNode r = new ConcurrentTraceNode(node, threadId);
			return List.of(r);
		}
		if (node.getReadVariables().size() > 0) {
			ConcurrentTraceNode readVariables = new ConcurrentTraceNode(node, threadId);
			readVariables.setReadVariables(node.getReadVariables());
			result.add(readVariables);
		}
		if (node.getWrittenVariables().size() > 0) {
			ConcurrentTraceNode writeVariables = new ConcurrentTraceNode(node, threadId);
			writeVariables.setWrittenVariables(node.getWrittenVariables());
			result.add(writeVariables);
		}
		return result;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Current threadId: " + this.currentTraceId + " " + super.toString();
	}
	
	
	
	
	
}
