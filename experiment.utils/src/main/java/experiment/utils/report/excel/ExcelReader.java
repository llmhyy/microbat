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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sav.common.core.SavRtException;

/**
 * @author LLT
 *
 */
public class ExcelReader {
	private InputStream in;
	protected Workbook workbook;
	private File file;
	private int headerRowNum;
	
	public ExcelReader(int headerRowNum) {
		this.headerRowNum = headerRowNum;
	}
	
	public ExcelReader(File file, int headerRowNum) {
		this(headerRowNum);
		reset(file);
	}

	public void reset(File file) {
		try {
			close();
			in = new FileInputStream(file);
			workbook = new XSSFWorkbook(in);
			this.file = file;
		} catch (IOException e) {
			throw new SavRtException(e);
		}
	}
	
	public int countRow(String sheetName) {
		Sheet sheet = workbook.getSheet(sheetName);
		int totalRow = sheet.getLastRowNum() - sheet.getFirstRowNum();
		return totalRow;
	}
	
	public List<String> listData(String sheetName, String columnHeader) {
		Sheet sheet = workbook.getSheet(sheetName);
		int col = getColumnIndex(sheet.getRow(sheet.getFirstRowNum()), columnHeader);
		List<String> data = new ArrayList<String>();
		if (col >= 0) {
			for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
				data.add(sheet.getRow(i).getCell(col).getStringCellValue());
			}
		}
		return data;
	}
	
	public List<Object> listObjData(String sheetName, String columnHeader) {
		Sheet sheet = workbook.getSheet(sheetName);
		List<Object> data = new ArrayList<Object>();
		if ((sheet == null) || (sheet.getPhysicalNumberOfRows() <= 0)) {
			return data;
		}
		int col = getColumnIndex(sheet.getRow(sheet.getFirstRowNum()), columnHeader);
		if (col >= 0) {
			for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
				data.add(getCellValue(sheet.getRow(i).getCell(col)));
			}
		}
		return data;
	}
	
	public List<List<Object>> listData(String sheetName) {
		List<List<Object>> data = new ArrayList<List<Object>>();
		Sheet sheet = workbook.getSheet(sheetName);
		for (int i = headerRowNum + 1; i <= sheet.getLastRowNum(); i++) {
			List<Object> rowData = new ArrayList<Object>();
			Row row = sheet.getRow(i);
			for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
				Cell cell = row.getCell(j);
				Object value = getCellValue(cell);
				rowData.add(value);
			}
			data.add(rowData);
		}
		
		return data;
	}
	
	public List<List<Object>> listData(String sheetName, List<String> newHeaders) {
		List<List<Object>> data = new ArrayList<List<Object>>();
		Map<Integer, Integer> headerIdxMap = initHeaderIdxMap(listHeader(sheetName), newHeaders);
		Sheet sheet = workbook.getSheet(sheetName);
		for (int i = headerRowNum + 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			int exceesiveCols = (row.getLastCellNum() - row.getFirstCellNum()) - headerIdxMap.size();
			if (exceesiveCols < 0) {
				exceesiveCols = 0;
			}
			int rowSize = newHeaders.size() + exceesiveCols;
			if (rowSize < row.getLastCellNum()) {
				rowSize = row.getLastCellNum();
			}
			Object[] rowData = new Object[rowSize];
			int extIdx = newHeaders.size();
			for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
				Cell cell = row.getCell(j);
				Object value = getCellValue(cell);
				Integer newIdx = headerIdxMap.get(j);
				if (newIdx == null) {
					newIdx = extIdx++;
				}
				rowData[newIdx] = value;
			}
			data.add(Arrays.asList(rowData));
		}
		
		return data;
	}

	private Map<Integer, Integer> initHeaderIdxMap(List<String> thisHeaders, List<String> newHeaders) {
		Map<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < thisHeaders.size(); i++) {
			int newIdx = newHeaders.indexOf(thisHeaders.get(i));
			if (newIdx >= 0) {
				map.put(i, newIdx);
			}  else {
				map.put(i, null);
			}
		}
		return map;
	}

	private Object getCellValue(Cell cell) {
		Object value = null;
		if (cell != null) {
			int cellType = cell.getCellType();
			if (cellType == Cell.CELL_TYPE_BOOLEAN) {
				value = cell.getBooleanCellValue();
			} else if (cellType == Cell.CELL_TYPE_STRING) {
				value = cell.getStringCellValue();
			} else if (cellType == Cell.CELL_TYPE_NUMERIC){
				value = cell.getNumericCellValue();
			}
		}
		return value;
	}
	
	public List<String> listSheetNames() {
		List<String> names = new ArrayList<String>(workbook.getNumberOfSheets());
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			names.add(workbook.getSheetName(i));
		}
		return names;
	}
	
	private int getColumnIndex(Row row, String columnHeader) {
		Iterator<Cell> cellIt = row.cellIterator();
		for (int idx = row.getFirstCellNum(); idx < row.getLastCellNum(); idx++) {
			Cell cell = cellIt.next();
			if (columnHeader.equals(cell.getStringCellValue())) {
				return idx;
			}
		}
		return -1;
	}
	
	public List<String> listHeader(String sheetName) {
		Sheet sheet = workbook.getSheet(sheetName);
		Row row = sheet.getRow(headerRowNum);
		List<String> headers = new ArrayList<String>(row.getLastCellNum() - row.getFirstCellNum());
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i);
			if (cell != null) {
				headers.add(cell.getStringCellValue());
			}
		}
		return headers;
	}

	public String getName() {
		return file.getName();
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public void close() throws IOException {
		if (in != null) {
			in.close();
		}
	}
}
