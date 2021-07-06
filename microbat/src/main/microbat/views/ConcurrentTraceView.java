package microbat.views;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReporter;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;

public class ConcurrentTraceView extends TraceView {

	protected Map<Long, Trace> traceMap;
	protected Trace curTrace;

	public Trace getCurrentTrace() {
		return curTrace;
	}

	public void setCurrentTrace(Trace curTrace) {
		this.curTrace = curTrace;
	}

	protected TreeViewer curTreeViewer;
	protected List<TreeViewer> viewerList;
	private List<Trace> traces;

	private Text searchText;
	private Button searchButton;
	private Composite tracePanel;
	private String previousSearchExpression = "";
	private boolean jumpFromSearch = false;

	public ConcurrentTraceView() {
	}

	public void setSearchText(String expression) {
		this.searchText.setText(expression);
		this.previousSearchExpression = expression;
	}

	private void createSearchBox(Composite parent) {
		Composite searchPanel = new Composite(parent, SWT.NONE);
		searchPanel.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.makeColumnsEqualWidth = false;
		searchPanel.setLayout(gridLayout);
		searchText = new Text(searchPanel, SWT.BORDER);
		searchText.setToolTipText(
				"search trace node by class name and line number, e.g., ClassName line:20 or just ClassName\n"
						+ "press \"enter\" for forward-search and \"shift+enter\" for backward-search.");
		FontData searchTextFont = searchText.getFont().getFontData()[0];
		searchTextFont.setHeight(10);
		searchText.setFont(new Font(Display.getCurrent(), searchTextFont));
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
		addSearchTextListener(searchText);
		searchButton = new Button(searchPanel, SWT.PUSH);
		GridData buttonData = new GridData(SWT.RIGHT, SWT.UP, false, false);
		// buttonData.widthHint = 50;
		searchButton.setLayoutData(buttonData);
		searchButton.setText("Go");
		addSearchButtonListener(searchButton);
	}

	protected void addSearchTextListener(final Text searchText) {
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 27 || e.character == SWT.CR) {

					boolean forward = (e.stateMask & SWT.SHIFT) == 0;

					String searchContent = searchText.getText();
					jumpToNode(searchContent, forward);
				}
			}
		});

	}

	protected void addSearchButtonListener(final Button serachButton) {
		searchButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String searchContent = searchText.getText();
				jumpToNode(searchContent, true);
			}
		});

	}

	public void jumpToNode(String searchContent, boolean next) {
		// Trace trace = Activator.getDefault().getCurrentTrace();

		if (!previousSearchExpression.equals(searchContent)) {
			curTrace.resetObservingIndex();
			previousSearchExpression = searchContent;
		}

		int selectionIndex = -1;
		if (next) {
			selectionIndex = curTrace.searchForwardTraceNode(searchContent);
		} else {
			selectionIndex = curTrace.searchBackwardTraceNode(searchContent);
		}
		// int selectionIndex = trace.searchBackwardTraceNode(searchContent);
		if (selectionIndex != -1) {
			this.jumpFromSearch = true;
			jumpToNode(curTrace, selectionIndex + 1, true);
		} else {
			MessageBox box = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell());
			box.setMessage("No more such node in trace!");
			box.open();
		}

	}

	/**
	 * indicate a node is selected by tool or human users.
	 */
	private boolean programmingSelection = false;

	/**
	 * indicate whether the program state should be refreshed when a trace node is
	 * selected programmatically.
	 */
	protected boolean refreshProgramState = true;

	public void jumpToNode(Trace trace, int order, boolean refreshProgramState) {
		assert order > 0;
		TraceNode node = trace.getExecutionList().get(order - 1);

		List<TraceNode> path = new ArrayList<>();
		while (node != null) {
			path.add(node);
			node = node.getAbstractionParent();
		}

		/** keep the original expanded list */
		Object[] expandedElements = curTreeViewer.getExpandedElements();
		for (Object obj : expandedElements) {
			TraceNode tn = (TraceNode) obj;
			path.add(tn);
		}

		TraceNode[] list = path.toArray(new TraceNode[0]);
		curTreeViewer.setExpandedElements(list);

		node = trace.getExecutionList().get(order - 1);

		programmingSelection = true;
		this.refreshProgramState = refreshProgramState;
		/**
		 * This step will trigger a callback function of node selection.
		 */
		curTreeViewer.setSelection(new StructuredSelection(node), true);
		programmingSelection = false;
		this.refreshProgramState = true;

		curTreeViewer.refresh();
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

			ReferenceAnnotation annotation = new ReferenceAnnotation(false,
					"Please check the status of this line");
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
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		createSearchBox(parent);
		// Add basic trace panel
		tracePanel = new Composite(parent, SWT.NONE);
		tracePanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		tracePanel.setLayout(layout);
	}

	public void creatPart(Composite parent, Trace trace) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new FillLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		String threadName = trace.getThreadName();
		group.setText(threadName != null ? threadName : "NA");
		TreeViewer viewer = new TreeViewer(group, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new TraceContentProvider());
		viewer.setLabelProvider(new TraceLabelProvider());
		viewerList.add(viewer);

		// Trace trace = Activator.getDefault().getCurrentTrace();
		viewer.setInput(trace);
		viewer.getTree().getVerticalBar().addListener(SWT.V_SCROLL, event -> {
			System.out.println(event);
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("unused")
			public void showDebuggingInfo(TraceNode node) {
				System.out.println("=========================");
				System.out.println("=========================");
				System.out.println("=========================");

				System.out.println("Data Dominator: ");
				for (TraceNode dominator : node.getDataDominators().keySet()) {
					VarValue var = node.getDataDominators().get(dominator);
					System.out.println(dominator);
					System.out.println("by: " + var);

					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				}

				System.out.println("=========================");

				System.out.println("Data Dominatee: " + node.getDataDominatee());
				for (TraceNode dominatee : node.getDataDominatee().keySet()) {
					VarValue var = node.getDataDominatee().get(dominatee);
					System.out.println(dominatee);
					System.out.println("by: " + var);

					System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				}

				// System.out.println("Control Dominator: ");
				// TraceNode controlDominator = node.getControlDominator();
				// System.out.println(controlDominator);
				// System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				//
				// System.out.println("Control Dominatee: ");
				// for(TraceNode dominatee: node.getControlDominatees()){
				// System.out.println(dominatee);
				// System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				// }

				System.out.println("Invocation Parent: ");
				System.out.println(node.getInvocationParent());
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");

				// System.out.println("Invocation Children: ");
				// for(TraceNode dominatee: node.getInvocationChildren()){
				// System.out.println(dominatee);
				// System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				// }

				System.out.println("Loop Parent: ");
				System.out.println(node.getLoopParent());
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");

				// System.out.println("Loop Children: ");
				// for(TraceNode dominatee: node.getLoopChildren()){
				// System.out.println(dominatee);
				// System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				// }

				System.out.println("Abstract Parent: ");
				System.out.println(node.getAbstractionParent());
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");

				// System.out.println("Abstract Children: ");
				// for(TraceNode dominatee: node.getAbstractChildren()){
				// System.out.println(dominatee);
				// System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				// }

				System.out.println();
				System.out.println();
			}

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection iSel = event.getSelection();
				if (iSel instanceof StructuredSelection) {
					StructuredSelection sel = (StructuredSelection) iSel;
					Object obj = sel.getFirstElement();

					if (obj instanceof TraceNode) {
						TraceNode node = (TraceNode) obj;

						// String simpleSig =
						// node.getMethodSign().substring(node.getMethodSign().indexOf("#")+1,
						// node.getMethodSign().length());
						// MethodFinderBySignature finder = new
						// MethodFinderBySignature(simpleSig);
						// ByteCodeParser.parse(node.getClassCanonicalName(), finder,
						// node.getTrace().getAppJavaClassPath());
						// System.currentTimeMillis();

						// showDebuggingInfo(node);

						if (!programmingSelection) {
							Behavior behavior = BehaviorData
									.getOrNewBehavior(Settings.launchClass);
							behavior.increaseAdditionalClick();
							new BehaviorReporter(Settings.launchClass)
									.export(BehaviorData.projectBehavior);
						}

						otherViewsBehavior(node);

						if (jumpFromSearch) {
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
						curTrace = traceMap.get(node.getTrace().getThreadId());
						curTrace.setObservingIndex(node.getOrder() - 1);
						curTreeViewer = getTreeViewerByThreadID(node.getTrace().getThreadId());
						curTreeViewer.getControl().setFocus();
					}
				}

			}

		});

		appendMenuForTraceStep(viewer);
	}

	public TreeViewer getTreeViewerByThreadID(long threadId) {
		for (TreeViewer tv : viewerList) {
			Trace t = (Trace) tv.getInput();
			if (t.getThreadId() == threadId) {
				return tv;
			}
		}
		return null;

	}

	protected Action createForSearchAction() {
		Action action = new Action() {
			public void run() {
				if (curTreeViewer.getSelection().isEmpty()) {
					return;
				}

				if (curTreeViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) curTreeViewer
							.getSelection();
					TraceNode node = (TraceNode) selection.getFirstElement();

					String className = node.getBreakPoint().getDeclaringCompilationUnitName();
					int lineNumber = node.getBreakPoint().getLineNumber();
					String searchString = Trace.combineTraceNodeExpression(className, lineNumber);
					ConcurrentTraceView.this.searchText.setText(searchString);
				}

			}

			public String getText() {
				return "for search";
			}
		};
		return action;
	}

	protected MenuManager menuMgr = new MenuManager("#PopupMenu");

	protected void appendMenuForTraceStep(TreeViewer viewer) {
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				Action action = createForSearchAction();
				menuMgr.add(action);

			}
		});

		viewer.getTree().setMenu(menuMgr.createContextMenu(viewer.getTree()));
	}

	protected void otherViewsBehavior(TraceNode node) {
		DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();

		if (this.refreshProgramState) {
			feedbackView.setTraceView(ConcurrentTraceView.this);
			feedbackView.refresh(node);
		}

		ReasonView reasonView = MicroBatViews.getReasonView();
		reasonView.refresh(feedbackView.getRecommender());

		markJavaEditor(node);
	}

	public void updateData() {

		Control[] childs = tracePanel.getChildren();
		for (int k = 0; k < childs.length; k++) {
			childs[k].dispose();
		}
		GridLayout layout = (GridLayout) tracePanel.getLayout();
		layout.numColumns = traces.size();
		layout.makeColumnsEqualWidth = true;
		// 1. add viewList for each trace
		// 2. add listener to each viewList
		// 3. point current ViewList and Trace when a even be catch
		viewerList = new ArrayList<>(traces.size());
		for (int i = 0; i < traces.size(); i++) {
			creatPart(tracePanel, traces.get(i));
		}

		for (TreeViewer viewer : viewerList) {
			viewer.refresh();
		}
		tracePanel.layout();
	}

	@Override
	public void setFocus() {

	}

	public void setTraceList(List<Trace> traces) {
		this.traces = traces;
		if (traceMap == null) {
			traceMap = new HashMap<>();
		}
		traceMap.clear();
		for (int i = 0; i < traces.size(); i++) {
			Trace trace = traces.get(i);
			traceMap.put(trace.getThreadId(), trace);
		}
	}

	class TraceContentProvider implements ITreeContentProvider {

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Trace) {
				Trace trace = (Trace) parentElement;
				// List<TraceNode> nodeList = trace.getExectionList();
				// List<TraceNode> nodeList = trace.getTopMethodLevelNodes();
				// List<TraceNode> nodeList = trace.getTopLoopLevelNodes();
				List<TraceNode> nodeList = trace.getTopAbstractionLevelNodes();
				return nodeList.toArray(new TraceNode[0]);
			} else if (parentElement instanceof TraceNode) {
				TraceNode parentNode = (TraceNode) parentElement;
				// List<TraceNode> nodeList = parentNode.getInvocationChildren();
				// List<TraceNode> nodeList = parentNode.getLoopChildren();
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
			if (element instanceof TraceNode) {
				TraceNode node = (TraceNode) element;
				// return !node.getInvocationChildren().isEmpty();
				// return !node.getLoopChildren().isEmpty();
				return !node.getAbstractChildren().isEmpty();
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

	}

	class TraceLabelProvider implements ILabelProvider {

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
			if (element instanceof TraceNode) {
				TraceNode node = (TraceNode) element;

				if (node.hasChecked()) {
					if (!node.isAllReadWrittenVarCorrect(true)) {
						return Settings.imageUI.getImage(ImageUI.WRONG_VALUE_MARK);
					} else if (node.isWrongPathNode()) {
						return Settings.imageUI.getImage(ImageUI.WRONG_PATH_MARK);
					} else {
						return Settings.imageUI.getImage(ImageUI.CHECK_MARK);
					}
				} else {
					return Settings.imageUI.getImage(ImageUI.QUESTION_MARK);
				}
			}

			return null;
		}

		public String getText(Object element) {
			if (element instanceof TraceNode) {
				TraceNode node = (TraceNode) element;
				BreakPoint breakPoint = node.getBreakPoint();
				// BreakPointValue programState = node.getProgramState();

				String className = breakPoint.getClassCanonicalName();
				if (className.contains(".")) {
					className = className.substring(className.lastIndexOf(".") + 1,
							className.length());
				}

				// String methodName = breakPoint.getMethodName();
				int lineNumber = breakPoint.getLineNumber();
				int order = node.getOrder();

				long duration = node.calulcateDuration();
				
				// TODO it is better to parse method name as well.
				// String message = className + "." + methodName + "(...): line " + lineNumber;
				String message = order + ". "
						+ MicroBatUtil.combineTraceNodeExpression(className, lineNumber, duration);
				return message;

			}

			return null;
		}

	}

}
