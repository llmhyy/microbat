package microbat.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import microbat.model.Entry;

public class ExecutionRangeTablePanel extends TableViewerEditablePanel<Entry> {

	public void setEntrys(List<Entry> entrys) {
		this.entrys = entrys;
	}

	private Table table;
	public List<Entry> entrys = new ArrayList<Entry>();

	public ExecutionRangeTablePanel(Composite parent, String title) {
		super(parent, title);
	}

	@Override
	protected void onAdd() {
		EntryInfoDialog dialog = new EntryInfoDialog(getShell(), null, EntryInfoDialog.ADD);
		dialog.create();
		if (dialog.open() == Window.OK) {
			Entry entry = dialog.getEntry();
			if (!entrys.contains(entry)) {
				entrys.add(entry);
				tableViewer.add(entry);
			}
		}
	}

	@Override
	protected TableViewer createTableViewer(Composite parent) {
		Composite tableContainer = new Composite(parent, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableContainer.setLayoutData(data);

		TableViewer tableViewer = super.createTableViewer(tableContainer);
		tableViewer.setContentProvider(initTableContentProvider());
		tableViewer.setLabelProvider(initTableLabelProvider());
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableContainer.setLayout(tableLayout);

		TableColumn tcClassName = new TableColumn(table, SWT.NONE);
		tableLayout.setColumnData(tcClassName, new ColumnPixelData(150, true, true));
		tcClassName.setText("classname");

		TableColumn tcStartLine = new TableColumn(table, SWT.NONE);
		tableLayout.setColumnData(tcStartLine, new ColumnPixelData(150, true, true));
		tcStartLine.setText("startline");

		TableColumn tcEndLine = new TableColumn(table, SWT.NONE);
		tableLayout.setColumnData(tcEndLine, new ColumnPixelData(150, true, true));
		tcEndLine.setText("endline");

		return tableViewer;
	}

	@Override
	protected void onRemove(List<Entry> elements) {
		super.onRemove(elements);
		entrys.removeAll(elements);
	}

	@Override
	protected boolean onEdit(Entry firstElement) {
		EntryInfoDialog dialog = new EntryInfoDialog(getShell(), firstElement, EntryInfoDialog.EDIT);
		dialog.create();
		if (dialog.open() == Window.OK) {
			Entry entry=dialog.getEntry();
			if(entry==null) {				
				try {
					throw new NullPointerException();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			entrys.remove(firstElement);
		    if (entrys.contains(entry)) {
		    	MessageDialog.openError(getShell(), "error", "Invalid modification");
		    	return false;
			}
			firstElement.setClassName(entry.getClassName());
			firstElement.setStartLine(entry.getStartLine());
			firstElement.setEndLine(entry.getEndLine());
			
		
		    entrys.add(firstElement);
			return true;
		}
		return false;
	}

	public List<Entry> getEntrys() {
		return entrys;
	}

	private IContentProvider initTableContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List) {
					return ((List<?>) inputElement).toArray();
				}

				return new Object[0];
			}
		};
	}

	private ITableLabelProvider initTableLabelProvider() {
		return new ITableLabelProvider() {
			@Override
			public void addListener(ILabelProviderListener listener) {

			}

			@Override
			public void dispose() {

			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {

			}

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				Entry entry = (Entry) element;
				switch (columnIndex) {
				case 0:
					return entry.getClassName();
				case 1:
					return String.valueOf(entry.getStartLine());
				case 2:
					return String.valueOf(entry.getEndLine());
				default:
					return "";
				}
			}

		};
	}
	
	public void setValue(List<Entry> value) {
		this.entrys=value;
		tableViewer.setInput(entrys);
	}

	class EntryInfoDialog extends TitleAreaDialog {

		private Text classNameText;
		private Text startLineText;
		private Text endLineText;
		private Shell shell;
		protected final static int ADD = 0;
		protected final static int EDIT = 1;
		private int opType;

		private Entry entry;

		public Entry getEntry() {
			return entry;
		}

		/**
		 * 
		 * @param parentShell
		 * @param enty
		 *            In add op should be null
		 * @param opType
		 *            dialog.ADD or dialog.EDIT
		 */
		public EntryInfoDialog(Shell parentShell, Entry enty, int opType) {
			super(parentShell);
			this.shell = parentShell;
			this.entry = enty;
			this.opType = opType;
		}

		@Override
		public void create() {
			super.create();
			setTitle("Entry Info");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(2, false);
			container.setLayout(layout);

			createClassName(container);
			createStartLine(container);
			createEndLine(container);

			if (this.entry != null && this.opType == EDIT) {
				classNameText.setText(entry.getClassName());
				startLineText.setText(String.valueOf(entry.getStartLine()));
				endLineText.setText(String.valueOf(entry.getEndLine()));
			}
			return area;
		}

		private void createClassName(Composite container) {
			Label className = new Label(container, SWT.NONE);
			className.setText("Class Name");

			GridData dataClassName = new GridData();
			dataClassName.grabExcessHorizontalSpace = true;
			dataClassName.horizontalAlignment = GridData.FILL;

			classNameText = new Text(container, SWT.BORDER);
			classNameText.setLayoutData(dataClassName);
		}

		private void createStartLine(Composite container) {
			Label startLine = new Label(container, SWT.NONE);
			startLine.setText("Start Line");

			GridData dataStartLine = new GridData();
			dataStartLine.grabExcessHorizontalSpace = true;
			dataStartLine.horizontalAlignment = GridData.FILL;
			startLineText = new Text(container, SWT.BORDER);
			startLineText.setLayoutData(dataStartLine);
		}

		private void createEndLine(Composite container) {
			Label endLine = new Label(container, SWT.NONE);
			endLine.setText("endline");

			GridData dataEndLine = new GridData();
			dataEndLine.grabExcessHorizontalSpace = true;
			dataEndLine.horizontalAlignment = GridData.FILL;
			endLineText = new Text(container, SWT.BORDER);
			endLineText.setLayoutData(dataEndLine);
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		// save content of the Text fields because they get disposed
		// as soon as the Dialog closes
		private boolean saveInput() {
			String className = classNameText.getText().trim();
			String startLine = startLineText.getText().trim();
			String endLine = endLineText.getText().trim();
			if (className.equals("") || startLine.equals("") || endLine.equals("")) {
				MessageDialog.openError(shell, "Incomplete information", "Entry information must be complete!");
				return false;
			}
			try {
				int startNo = Integer.valueOf(startLine);
				int endNo = Integer.valueOf(endLine);
				if (startNo > endNo) {
					MessageDialog.openError(shell, "Wrong content", "The line number should be in ascending order!");
					return false;
				}
			} catch (Exception e) {
				MessageDialog.openError(super.getShell(), "Wrong content",
						"Make sure the line number is correct !");
				return false;
			}

			entry = new Entry();
			entry.setClassName(className);
			entry.setStartLine(Integer.valueOf(startLine));
			entry.setEndLine(Integer.valueOf(endLine));
			
			return true;

		}
		

		@Override
		protected void okPressed() {
			if (saveInput()) {
				super.okPressed();
			}
		}

	}
}
