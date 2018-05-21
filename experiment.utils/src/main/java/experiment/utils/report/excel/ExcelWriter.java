/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package experiment.utils.report.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class ExcelWriter {
	protected Workbook workbook;
	private File file;
	
	protected ExcelWriter() {
	}
	
	public ExcelWriter(File file) {
		reset(file);
	}

	public void reset(File file) {
		this.file = file;
		try {
			if (!file.exists()) {
				initFromNewFile(file);
				writeWorkbook();
			} else {
				initFromExistingFile(file);
			}
		} catch (Exception e) {
			throw new SavRtException(e);
		}
	}
	
	protected void initFromExistingFile(File file) throws Exception {
		InputStream inp = new FileInputStream(file);
		workbook = WorkbookFactory.create(inp);
	}

	protected void initFromNewFile(File file) {
		workbook = new XSSFWorkbook();
	}

	public Sheet createSheet(String name) {
		return workbook.createSheet(name);
	}
	
	public Sheet createSheet(String name, String[] headers, int headerRowIdx) {
		Sheet sheet = createSheet(name);
		initDataSheetHeader(sheet, headers, headerRowIdx);
		return sheet;
	}
	
	public void initDataSheetHeader(Sheet sheet, String[] headers, int headerRowIdx) {
		Row headerRow = newDataSheetRow(sheet, headerRowIdx);
		int idx = 0;
		for (String header : headers) {
			headerRow.createCell(idx++).setCellValue(header);
		}
	}
	
	protected Row newDataSheetRow(Sheet dataSheet, int headerRowIdx) {
		return dataSheet.createRow(headerRowIdx);
	}
	
	public void writeWorkbook() throws IOException{
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			workbook.write(out); 
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	public void addCell(Row row, int cellIdx, double value) {
		if (!Double.isNaN(value)) {
			row.createCell(cellIdx).setCellValue(value);
		} 
	}

	public void addCell(Row row, int cellIdx, String value) {
		row.createCell(cellIdx).setCellValue(value);
	}
	
	public Sheet getSheet(String name, String[] headers, int headerRowIdx) {
		Sheet sheet = workbook.getSheet(name);
		if (sheet == null) {
			sheet = createSheet(name, headers, headerRowIdx);
		}
		return sheet;
	}

	public void writeSheet(String sheetName, List<List<Object>> data) throws IOException {
		Sheet sheet = getSheet(sheetName);
		int rowNum = sheet.getLastRowNum() + 1;
		for (List<Object> rowData : data) {
			rowNum = fillRowData(sheet, rowNum, rowData);
		}
		
		writeWorkbook();
	}

	private int fillRowData(Sheet sheet, int rowNum, List<Object> rowData) {
		Row row = sheet.createRow(rowNum++);
		int cellNum = 0;
		for (Object value : rowData) {
			if (value instanceof Boolean) {
				row.createCell(cellNum).setCellValue((Boolean)value);
			} else if (value instanceof String) {
				row.createCell(cellNum).setCellValue((String)value);
			} else if (value instanceof Number) {
				row.createCell(cellNum).setCellValue(Double.valueOf(value.toString()));
			}
			cellNum++;
		}
		return rowNum;
	}
	
	private int fillRowData(CellStyle cellStyle, Sheet sheet, int rowNum, List<Object> rowData,
			List<Integer> highlightCols) {
		Row row = sheet.createRow(rowNum++);
		row.setRowStyle(cellStyle);
		for (int i = 0; i < rowData.size(); i++) {
			Cell cell = row.createCell(i);
			Object value = rowData.get(i);
			if (value  instanceof Boolean) {
				cell.setCellValue((Boolean)value);
			} else if (value instanceof String) {
				cell.setCellValue((String)value);
			} else if (value instanceof Number) {
				cell.setCellValue(Double.valueOf(value.toString()));
			}
			if (highlightCols == null || highlightCols.contains(i)) {
				cell.setCellStyle(cellStyle);
			}
		}
		return rowNum;
	}

	private int fillRowData(CellStyle cellStyle, Sheet sheet, int rowNum, List<Object> rowData) {
		Row row = sheet.createRow(rowNum++);
		row.setRowStyle(cellStyle);
		int cellNum = 0;
		for (Object value : rowData) {
			Cell cell = row.createCell(cellNum++);
			if (value instanceof Boolean) {
				cell.setCellValue((Boolean)value);
			} else if (value instanceof String) {
				cell.setCellValue((String)value);
			} else if (value instanceof Number) {
				cell.setCellValue(Double.valueOf(value.toString()));
			}
			cell.setCellStyle(cellStyle);
		}
		return rowNum;
	}

	public void writeSheet(String sheetName, List<List<Object>> data, IndexedColors backgroundColor,
			IndexedColors fontColor) throws IOException {
		CellStyle cellStyle = workbook.createCellStyle();
		if (backgroundColor != null) {
			cellStyle.setFillBackgroundColor(backgroundColor.index);
		}
		if (fontColor != null) {
			Font font = workbook.createFont();
			font.setColor(fontColor.getIndex());
			cellStyle.setFont(font);
		}
		Sheet sheet = getSheet(sheetName);
		int rowNum = sheet.getLastRowNum() + 1;
		for (List<Object> rowData : data) {
			fillRowData(cellStyle, sheet, rowNum++, rowData);
		}
		
		writeWorkbook();
	}
	
	public void writeRecordDiffs(String sheetName, String[] mergedHeaders, List<RecordDiff> records, IndexedColors backgroundColor,
			IndexedColors highlightColor) throws IOException {
		if (CollectionUtils.isEmpty(records)) {
			return;
		}
		CellStyle oldRecordCellStyle = workbook.createCellStyle();
		if (backgroundColor != null) {
			oldRecordCellStyle.setFillBackgroundColor(backgroundColor.index);
			oldRecordCellStyle.setFillForegroundColor(backgroundColor.index);
			oldRecordCellStyle.setFillPattern(CellStyle.BIG_SPOTS);
		}
		CellStyle newRecordCellStyle = workbook.createCellStyle();
		if (highlightColor != null) {
			Font font = workbook.createFont();
			font.setColor(highlightColor.getIndex());
			newRecordCellStyle.setFont(font);
		}
		Sheet sheet = getSheet(sheetName);
		int rowNum = sheet.getLastRowNum() + 1;
		for (RecordDiff record : records) {
			rowNum = fillRowData(oldRecordCellStyle, sheet, rowNum, record.getOldRecord().getCellValues());
			rowNum = fillRowData(newRecordCellStyle, sheet, rowNum, record.getNewRecord().getCellValues(), record.getDiffCols());
		}

		writeWorkbook();
	}
	
	public void writeNewRecord(String sheetName, String[] mergedHeaders, List<RecordDiff> records,
			IndexedColors textColor) throws IOException {
		if (CollectionUtils.isEmpty(records)) {
			return;
		}
		CellStyle newRecordCellStyle = workbook.createCellStyle();
		if (textColor != null) {
			Font font = workbook.createFont();
			font.setColor(textColor.getIndex());
			newRecordCellStyle.setFont(font);
		}
		Sheet sheet = getSheet(sheetName);
		int rowNum = sheet.getLastRowNum() + 1;
		for (RecordDiff record : records) {
			rowNum = fillRowData(newRecordCellStyle, sheet, rowNum, record.getNewRecord().getCellValues(), null);
		}

		writeWorkbook();
	}
	
	private Sheet getSheet(String sheetName) {
		Sheet sheet = workbook.getSheet(sheetName);
		if (sheet == null) {
			sheet = workbook.createSheet(sheetName);
		}
		return sheet;
	}
	
}
