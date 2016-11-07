package microbat.views;

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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import microbat.Activator;
import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReporter;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;

public class TraceView extends ViewPart {
	
	private TreeViewer listViewer;
	private Text searchText;
	private Button searchButton;
	
	private String previousSearchExpression = "";
	private boolean jumpFromSearch = false;

	public TraceView() {
	}
	
	public void setSearchText(String expression){
		this.searchText.setText(expression);
		this.previousSearchExpression = expression;
	}
	
	private void createSearchBox(Composite parent){
		searchText = new Text(parent, SWT.BORDER);
		FontData searchTextFont = searchText.getFont().getFontData()[0];
		searchTextFont.setHeight(10);
		searchText.setFont(new Font(Display.getCurrent(), searchTextFont));
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		addSearchTextListener(searchText);
		
		searchButton = new Button(parent, SWT.PUSH);
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, false, false);
		//buttonData.widthHint = 50;
		searchButton.setLayoutData(buttonData);
		searchButton.setText("Go");
		addSearchButtonListener(searchButton);
	}
	
	protected void addSearchTextListener(final Text searchText) {
		searchText.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == 27 || e.character == SWT.CR){
					
					boolean forward = (e.stateMask & SWT.SHIFT) == 0;
					
					String searchContent = searchText.getText();
					jumpToNode(searchContent, forward);
				}
			}
		});
		
	}
	
	protected void addSearchButtonListener(final Button serachButton) {
		searchButton.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDown(MouseEvent e) {
				String searchContent = searchText.getText();
				jumpToNode(searchContent, true);
			}
		});
		
	}
	
	public void jumpToNode(String searchContent, boolean forward){
		Trace trace = Activator.getDefault().getCurrentTrace();
		
		if(!previousSearchExpression.equals(searchContent)){
			trace.resetObservingIndex();
			previousSearchExpression = searchContent;
		}
		
		int selectionIndex = -1; 
		if(forward){
			selectionIndex = trace.searchForwardTraceNode(searchContent);			
		}
		else{
			selectionIndex = trace.searchBackwardTraceNode(searchContent);
		}
//		int selectionIndex = trace.searchBackwardTraceNode(searchContent);
		if(selectionIndex != -1){
			this.jumpFromSearch = true;
			jumpToNode(trace, selectionIndex+1);
		}
		else{
			MessageBox box = new MessageBox(PlatformUI.getWorkbench()
					.getDisplay().getActiveShell());
			box.setMessage("No more such node in trace!");
			box.open();
		}
		
	}
	
	private boolean programmingSelection = false;
	
	public void jumpToNode(Trace trace, int order){
		TraceNode node = trace.getExectionList().get(order-1);
		
		
		List<TraceNode> path = new ArrayList<>();
		while(node != null){
			path.add(node);
			node = node.getAbstractionParent();
		}
		
		/** keep the original expanded list */
		Object[] expandedElements = listViewer.getExpandedElements();
		for(Object obj: expandedElements){
			TraceNode tn = (TraceNode)obj;
			path.add(tn);
		}
		
		TraceNode[] list = path.toArray(new TraceNode[0]);
		listViewer.setExpandedElements(list);
		
		node = trace.getExectionList().get(order-1);
		
		programmingSelection = true;
		listViewer.setSelection(new StructuredSelection(node), true);	
		programmingSelection = false;
		
		listViewer.refresh();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
		
		createSearchBox(parent);
		
		listViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		listViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		listViewer.setContentProvider(new TraceContentProvider());
		listViewer.setLabelProvider(new TraceLabelProvider());
		
		Trace trace = Activator.getDefault().getCurrentTrace();
		listViewer.setInput(trace);
		
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@SuppressWarnings("unused")
			public void showDebuggingInfo(TraceNode node){
				System.out.println("=========================");
				System.out.println("=========================");
				System.out.println("=========================");
				
				Trace t = Activator.getDefault().getCurrentTrace();
//				System.out.println("Data Dominator: ");
//				for(TraceNode dominator: node.getDataDominator().keySet()){
//					List<String> varIDs = node.getDataDominator().get(dominator);
//					System.out.println(dominator);
//					System.out.println("by: ");
//					
//					for(String varID: varIDs){
//						StepVariableRelationEntry entry = t.getStepVariableTable().get(varID);
//						System.out.println(varID + ":" + entry.getAliasVariables());
//					}
//					
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				}
//				
//				System.out.println("=========================");
//				
//				System.out.println("Data Dominatee: " + node.getDataDominatee());
//				for(TraceNode dominatee: node.getDataDominatee().keySet()){
//					List<String> varIDs = node.getDataDominatee().get(dominatee);
//					System.out.println(dominatee);
//					System.out.println("by: ");
//					
//					for(String varID: varIDs){
//						StepVariableRelationEntry entry = t.getStepVariableTable().get(varID);
//						System.out.println(varID + ":" + entry.getAliasVariables());
//					}
//					
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				}
				
//				System.out.println("Control Dominator: ");
//				TraceNode controlDominator = node.getControlDominator();
//				System.out.println(controlDominator);	
//				System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				
//				System.out.println("Control Dominatee: ");
//				for(TraceNode dominatee: node.getControlDominatees()){
//					System.out.println(dominatee);
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				}
				
				System.out.println("Invocation Parent: ");
				System.out.println(node.getInvocationParent());
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				
//				System.out.println("Invocation Children: ");
//				for(TraceNode dominatee: node.getInvocationChildren()){
//					System.out.println(dominatee);
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				}
				
				System.out.println("Loop Parent: ");
				System.out.println(node.getLoopParent());
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				
//				System.out.println("Loop Children: ");
//				for(TraceNode dominatee: node.getLoopChildren()){
//					System.out.println(dominatee);
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				}
				
				System.out.println("Abstract Parent: ");
				System.out.println(node.getAbstractionParent());
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				
//				System.out.println("Abstract Children: ");
//				for(TraceNode dominatee: node.getAbstractChildren()){
//					System.out.println(dominatee);
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
//				}
				
				
				System.out.println();
				System.out.println();
			}
			
			public void selectionChanged(SelectionChangedEvent event) {
				try {
					ISelection iSel = event.getSelection();
					if(iSel instanceof StructuredSelection){
						StructuredSelection sel = (StructuredSelection)iSel;
						Object obj = sel.getFirstElement();
						
						if(obj instanceof TraceNode){
							TraceNode node = (TraceNode)obj;
							
							showDebuggingInfo(node);
							
							if(!programmingSelection){
								Behavior behavior = BehaviorData.getOrNewBehavior(Settings.lanuchClass);
								behavior.increaseAdditionalClick();
								new BehaviorReporter(Settings.lanuchClass).export(BehaviorData.projectBehavior);
							}
							
							DebugFeedbackView view = (DebugFeedbackView)PlatformUI.getWorkbench().
									getActiveWorkbenchWindow().getActivePage().showView(MicroBatViews.DEBUG_FEEDBACK);
							view.refresh(node);

							markJavaEditor(node);
							
							if(jumpFromSearch){
								jumpFromSearch = false;

								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										try {
											Thread.sleep(300);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										searchText.setFocus();
									}
								});
								
							}
							else{
								listViewer.getTree().setFocus();								
							}
							
							Activator.getDefault().getCurrentTrace().setObservingIndex(node.getOrder()-1);
						}
					}
					
				} catch (PartInitException e) {
					e.printStackTrace();
				}
				
			}

			@SuppressWarnings("unchecked")
			private void markJavaEditor(TraceNode node) {
				BreakPoint breakPoint = node.getBreakPoint();
				String qualifiedName = breakPoint.getClassCanonicalName();
				ICompilationUnit javaUnit = JavaUtil.findICompilationUnitInProject(qualifiedName);
				
				try {
					ITextEditor sourceEditor = (ITextEditor) JavaUI.openInEditor(javaUnit);
					AnnotationModel annotationModel = (AnnotationModel)sourceEditor.getDocumentProvider().
							getAnnotationModel(sourceEditor.getEditorInput());
					/**
					 * remove all the other annotations
					 */
					Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
					while(annotationIterator.hasNext()) {
						Annotation currentAnnotation = annotationIterator.next();
						annotationModel.removeAnnotation(currentAnnotation);
					}	
					
					IFile javaFile = (IFile)sourceEditor.getEditorInput().getAdapter(IResource.class);
					IDocumentProvider provider = new TextFileDocumentProvider();
					provider.connect(javaFile);
					IDocument document = provider.getDocument(javaFile);
					IRegion region = document.getLineInformation(breakPoint.getLineNumber()-1);
					
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
		});
	}
	
	public void updateData(){
		Trace trace = Activator.getDefault().getCurrentTrace();
		listViewer.setInput(trace);
		listViewer.refresh();
	}

	@Override
	public void setFocus() {
		
	}
	
	class TraceContentProvider implements ITreeContentProvider{

		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof Trace){
				Trace trace = (Trace)parentElement;
				//List<TraceNode> nodeList = trace.getExectionList();
//				List<TraceNode> nodeList = trace.getTopMethodLevelNodes();
//				List<TraceNode> nodeList = trace.getTopLoopLevelNodes();
				List<TraceNode> nodeList = trace.getTopAbstractionLevelNodes();
				return nodeList.toArray(new TraceNode[0]);
			}
			else if(parentElement instanceof TraceNode){
				TraceNode parentNode = (TraceNode)parentElement;
//				List<TraceNode> nodeList = parentNode.getInvocationChildren();
//				List<TraceNode> nodeList = parentNode.getLoopChildren();
				List<TraceNode> nodeList = parentNode.getAbstractChildren();
				return nodeList.toArray(new TraceNode[0]);
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if(element instanceof TraceNode){
				TraceNode node = (TraceNode)element;
//				return !node.getInvocationChildren().isEmpty();
//				return !node.getLoopChildren().isEmpty();
				return !node.getAbstractChildren().isEmpty();
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

	}
	
	class TraceLabelProvider implements ILabelProvider{

		public void addListener(ILabelProviderListener listener) {
			
		}

		public void dispose() {
			
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			
		}

		public Image getImage(Object element) {
			if(element instanceof TraceNode){
				TraceNode node = (TraceNode)element;
				
				if(node.hasChecked()){
					if(!node.isAllReadWrittenVarCorrect(true)){
						return Settings.imageUI.getImage(ImageUI.WRONG_VALUE_MARK);	
					}
					else if(node.isWrongPathNode()){
						return Settings.imageUI.getImage(ImageUI.WRONG_PATH_MARK);	
					}
					else {
						return Settings.imageUI.getImage(ImageUI.CHECK_MARK);
					}
				}
				else{
					return Settings.imageUI.getImage(ImageUI.QUESTION_MARK);
				}
			}
			
			return null;
		}

		public String getText(Object element) {
			if(element instanceof TraceNode){
				TraceNode node = (TraceNode)element;
				BreakPoint breakPoint = node.getBreakPoint();
//				BreakPointValue programState = node.getProgramState();
				
				String className = breakPoint.getClassCanonicalName();
				if(className.contains(".")){
					className = className.substring(className.lastIndexOf(".")+1, className.length());
				}
				
//				String methodName = breakPoint.getMethodName();
				int lineNumber = breakPoint.getLineNumber();
				int order = node.getOrder();
				
				//TODO it is better to parse method name as well.
//				String message = className + "." + methodName + "(...): line " + lineNumber;
				String message = order + ". " + MicroBatUtil.combineTraceNodeExpression(className, lineNumber);
				return message;
				
			}
			
			return null;
		}
		
	}

}
