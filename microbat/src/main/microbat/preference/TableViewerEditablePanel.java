/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.preference;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import microbat.util.SWTFactory;

/**
 * @author LLT
 *
 */
public abstract class TableViewerEditablePanel<T> {
	public static final int ADD_BTN = 0;
	public static final int EDIT_BTN = 1;
	public static final int REMOVE_BTN = 2;
	protected Composite panel;
	protected TableViewer tableViewer;
	protected Composite btnGroup;
	protected Button addBtn;
	protected Button editBtn;
	protected Button removeBtn; 
	private String title;
	
	public TableViewerEditablePanel(Composite parent, String title) {
		this.title = title;
		/* table on the left */
		panel = createContentPanel(parent);
		GridData data;
		tableViewer = createTableViewer(panel);
		
		/* butons on the right */
		btnGroup = new Composite(panel, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		btnGroup.setLayout(layout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		btnGroup.setLayoutData(data);
		addButtons();
		registerListener();
		setToInitState();
	}

	protected void addButtons() {
		addBtn = SWTFactory.createBtnAlignFill(btnGroup, "Add...");
		editBtn = SWTFactory.createBtnAlignFill(btnGroup, "Edit...");
		removeBtn = SWTFactory.createBtnAlignFill(btnGroup, "Remove");
	}
	
	protected void setToInitState() {
		editBtn.setEnabled(false);
		removeBtn.setEnabled(false);
	}

	protected TableViewer createTableViewer(Composite parent) {
		TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		tableViewer.getTable().setLayoutData(data);
		return tableViewer;
	}

	protected Composite createContentPanel(Composite parent) {
		Composite group = SWTFactory.createGridPanel(parent, 1);
		SWTFactory.createLabel(group, title);
		return SWTFactory.createGridPanel(group, 2);
	}
	
	@SuppressWarnings("unchecked")
	private void registerListener() {
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event
						.getSelection();
				onSelectTableRow(selection);
			}

		});
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAdd();
			}
		});
		
		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				T firstElement = (T)((StructuredSelection)tableViewer
						.getSelection()).getFirstElement();
				if (onEdit(firstElement)) {
					tableViewer.refresh(firstElement);
				}
			}
		});
		
		removeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<T> elements = ((StructuredSelection) tableViewer
						.getSelection()).toList();
				onRemove(elements);
			}
		});
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	protected void onSelectTableRow(StructuredSelection selection) {
		int size = selection.size();
		editBtn.setEnabled(size == 1);
		removeBtn.setEnabled(size > 0);
	}
	
	protected void onRemove(List<T> elements) {
		tableViewer.remove(elements.toArray());
	}

	protected boolean onEdit(T firstElement) {
		return false;
	}

	protected abstract void onAdd();

	/**
	 * @param kind 
	 * 		TableViewerEditablePanel.ADD_BTN
	 * 		TableViewerEditablePanel.EDIT_BTN
	 * 		TableViewerEditablePanel.REMOVE_BTN	
	 */
	public void hide(int kind) {
		Button btn = getButton(kind);
		hide(btn);
	}

	protected void hide(Button btn) {
		btn.setVisible(false);
		if (btn != removeBtn) {
			btn.moveBelow(removeBtn);
		}
	}
	
	/**
	 * @param kind 
	 * 		TableViewerEditablePanel.ADD_BTN
	 * 		TableViewerEditablePanel.EDIT_BTN
	 * 		TableViewerEditablePanel.REMOVE_BTN	
	 */
	public Button getButton(int kind) {
		if (kind == ADD_BTN) {
			return addBtn;
		}
		if (kind == REMOVE_BTN) {
			return removeBtn;
		}
		if (kind == EDIT_BTN) {
			return editBtn;
		}
		throw new IllegalArgumentException(
				"Cannot find the button with kind = " + kind);
	}
	
	public Button getAddBtn() {
		return addBtn;
	}
	
	public Button getRemoveBtn() {
		return removeBtn;
	}
	
	public GridData getTableLayoutData() {
		return (GridData)tableViewer.getTable().getLayoutData();
	}
	
	public Composite getWidget() {
		return panel;
	}
	
	protected Shell getShell() {
		return panel.getShell();
	}
}
