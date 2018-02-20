/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.progress.IProgressService;

import microbat.util.FilterUtils;
import microbat.util.JavaUtil;
import microbat.util.SWTFactory;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StringUtils;

/**
 * @author LLT
 *
 */
public class AnalysisScopesTablePanel extends TableViewerEditablePanel<String> {
	private Button addFilterTextBtn;
	private Button addPackageBtn;
	private Button addTypeBtn;
	private List<String> filterTexts = new ArrayList<String>();
	private boolean isExcludeTable;
	
	public AnalysisScopesTablePanel(Composite parent, String title, boolean isExcludeTable) {
		super(parent, title);
		this.isExcludeTable = isExcludeTable;
	}
	
	@Override
	protected Composite createContentPanel(Composite parent) {
		Composite content = super.createContentPanel(parent);
		((GridData) content.getLayoutData()).widthHint = 600;
		return content;
	}
	
	@Override
	protected TableViewer createTableViewer(Composite parent) {
		Composite tableContainer = new Composite(parent, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 150;
		tableContainer.setLayoutData(data);
		TableViewer tableViewer = super.createTableViewer(tableContainer);
		tableViewer.getTable().setHeaderVisible(false);
		tableViewer.setContentProvider(initTableContentProvider());
		/* libs */
		TableViewerColumn typeCol = new TableViewerColumn(tableViewer, SWT.NONE);
		typeCol.setLabelProvider(initTypeLabelProvider());
		
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(typeCol.getColumn(), new ColumnWeightData(300));
		tableContainer.setLayout(tableLayout);
		
		return tableViewer;
	}
	
	@Override
	protected void addButtons() {
		addFilterTextBtn = SWTFactory.createBtnAlignFill(btnGroup, "Add Filter Text...");
		addFilterTextBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				String hint;
				if (isExcludeTable) {
					hint = "Ex: \njava.util.* : exclude all types and packages under package java.util, "
							+ "\njava.util.Arrays : exclude type Arrays";
				} else {
					hint = "Ex: \njava.util.* : include all types and packages under package java.util, "
							+ "\njava.util.*\\ : include all types only under package java.util, "
							+ "\njava.util.Arrays : include type Arrays only"
							+ "\njava.util.Arrays* : include type Arrays and its inner types";
				}
				InputDialog dialog = new InputDialog(getShell(), "Add Filter Text", hint, "", null);
				if (dialog.open() == Window.OK) {
					if (StringUtils.isEmpty(dialog.getValue())) {
						return;
					}
					onAdd(dialog.getValue());
				}    
			}
		});
		addBtn = addPackageBtn = SWTFactory.createBtnAlignFill(btnGroup, "Add Package...");
		addTypeBtn = SWTFactory.createBtnAlignFill(btnGroup, "Add Type...");
		editBtn = SWTFactory.createBtnAlignFill(btnGroup, "Edit...");
		removeBtn = SWTFactory.createBtnAlignFill(btnGroup, "Remove");
		
		addPackageBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				addPackage();
			}
		});
		
		addTypeBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				addType();
			}
		});
		hide(EDIT_BTN);
	}
	
	protected void addType() {
		try {
			SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), PlatformUI.getWorkbench().getProgressService(), 
					JavaUtil.getSpecificJavaProjectInWorkspace(), IJavaElementSearchConstants.CONSIDER_CLASSES, true);
			if (dialog.open() == Window.OK) {
				if (CollectionUtils.isEmpty(dialog.getResult())) {
					return;
				}
				for (Object obj : dialog.getResult()) {
					IType jele = (IType) obj;
					String filterText = jele.getFullyQualifiedName('.');
					onAdd(filterText);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private void onAdd(String filterText) {
		if (!filterTexts.contains(filterText)) {
			tableViewer.add(filterText);
			filterTexts.add(filterText);
		}
	}

	private void addPackage() {
		IProgressService context = PlatformUI.getWorkbench().getProgressService();
		IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(
				new IJavaElement[] { JavaCore.create(JavaUtil.getSpecificJavaProjectInWorkspace()) });
		SelectionDialog dialog = JavaUI.createPackageDialog(getShell(), context, searchScope, true, true, "");
		if (dialog.open() == Window.OK) {
			if (CollectionUtils.isEmpty(dialog.getResult())) {
				return;
			}
			for (Object obj : dialog.getResult()) {
				IJavaElement jele = (IJavaElement) obj;
				String filterText = FilterUtils.toFilterText(jele.getElementName());
				onAdd(filterText);
			}
		}             
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
				return new Object[] { inputElement };
			}
		};
	}
	
	private CellLabelProvider initTypeLabelProvider() {
		return new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				cell.setText(cell.getElement().toString());
			}
		};
	}
	
	@Override
	protected void onRemove(List<String> elements) {
		super.onRemove(elements);
		filterTexts.removeAll(elements);
	}

	@Override
	protected void onAdd() {
		// ignore
	}

	public void setValue(String[] value) {
		filterTexts = CollectionUtils.toArrayList(value);
		tableViewer.setInput(filterTexts);
	}

	public List<String> getFilterTexts() {
		return filterTexts;
	}
}
