package microbat.views;

import java.util.Iterator;

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
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import microbat.concurrent.model.ConcurrentTrace;
import microbat.concurrent.model.ConcurrentTraceNode;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.JavaUtil;

/**
 * View used for rendering generated sequential conccurrent trace
 * @author Gabau
 *
 */
public class SequentialConcurrentView extends TraceView {
	// todo: shift these out of this file
	private static class SequentialConcurrentViewProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof java.util.List<?>)) {
				return new Object[0];
			}
			// TODO Auto-generated method stub
			java.util.List<ConcurrentTraceNode> traceNodes = (java.util.List<ConcurrentTraceNode>) inputElement;
			return traceNodes.toArray();
		}
		
	}
	
	private static class SequentialConcurrentLabelProvider extends StyledCellLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			super.addListener(listener);
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return super.isLabelProperty(element, property);
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			super.removeListener(listener);
		}

		@Override
		protected void fireLabelProviderChanged(LabelProviderChangedEvent event) {
			// TODO Auto-generated method stub
			super.fireLabelProviderChanged(event);
		}
		
	}
	protected ConcurrentTrace inputTrace;
	protected ListViewer listViewer;
	protected SequentialConcurrentViewProvider provider = 
			new SequentialConcurrentViewProvider();
	protected IBaseLabelProvider labelProvider = new SequentialConcurrentLabelProvider();
	
	public SequentialConcurrentView() {
		
	}
	
	
	
	
	@Override
	public void jumpToNode(String searchContent, boolean next) {
		// TODO Auto-generated method stub
	}




	@Override
	public void jumpToNode(Trace trace, int order, boolean refreshProgramState) {
		// TODO Auto-generated method stub
		this.jumpToNode(trace.getTraceNode(order));
	}




	@Override
	public void jumpToNode(TraceNode node) {
		// TODO Auto-generated method stub
		otherViewsBehavior(node);
		listViewer.setSelection(
				new StructuredSelection(node), true);
	}




	public void setTraceNodes(ConcurrentTrace trace) {
		this.inputTrace = trace;
	}
	
	@Override
	public void updateData() {
		listViewer.setInput(inputTrace.getSequentialTrace());
		listViewer.refresh();
		
	}
	
	
	@Override
	public Trace getCurrentTrace() {
		return this.inputTrace;
	}

	@Override
	public Trace getTrace() {
		// TODO Auto-generated method stub
		return this.inputTrace;
	}


	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		this.listViewer = new ListViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewer.setContentProvider(provider);
		listViewer.setLabelProvider(labelProvider);
		listViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
			    IStructuredSelection selection = listViewer.getStructuredSelection();
			    ConcurrentTraceNode firstElement = (ConcurrentTraceNode) selection.getFirstElement();
			    markJavaEditor(firstElement);
			    otherViewsBehavior(firstElement);
			
			}
		});
	}

	@Override
	protected void otherViewsBehavior(TraceNode node) {
		DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();
		feedbackView.setTraceView(this);
		feedbackView.refresh(node);
		
		
		DebugPilotFeedbackView debugPilotFeedbackView = MicroBatViews.getDebugPilotFeedbackView();
		debugPilotFeedbackView.refresh(node, this.inputTrace);
		

//		ReasonView reasonView = MicroBatViews.getReasonView();
//		reasonView.refresh(feedbackView.getRecommender());

		markJavaEditor(node);
	}
	
	@SuppressWarnings("unchecked")
	protected void markJavaEditor(TraceNode node) {
		BreakPoint breakPoint = node.getBreakPoint();
		String qualifiedName = breakPoint.getClassCanonicalName();
		ICompilationUnit javaUnit = JavaUtil.findICompilationUnitInProject(qualifiedName);

		if (javaUnit == null) {
			return;
		}

		try {
			ITextEditor sourceEditor = (ITextEditor) JavaUI.openInEditor(javaUnit);
			AnnotationModel annotationModel = (AnnotationModel) sourceEditor.getDocumentProvider()
					.getAnnotationModel(sourceEditor.getEditorInput());
			/**
			 * remove all the other annotations
			 */
			Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
			while (annotationIterator.hasNext()) {
				Annotation currentAnnotation = annotationIterator.next();
				annotationModel.removeAnnotation(currentAnnotation);
			}

			IFile javaFile = (IFile) sourceEditor.getEditorInput().getAdapter(IResource.class);
			IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(javaFile);
			IDocument document = provider.getDocument(javaFile);
			IRegion region = document.getLineInformation(breakPoint.getLineNumber() - 1);

			if (region != null) {
				sourceEditor.selectAndReveal(region.getOffset(), 0);
			}

			ReferenceAnnotation annotation = new ReferenceAnnotation(false, "Please check the status of this line");
			Position position = new Position(region.getOffset(), region.getLength());

			annotationModel.addAnnotation(annotation, position);

		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
	
	

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
