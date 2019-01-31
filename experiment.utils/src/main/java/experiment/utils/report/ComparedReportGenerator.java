/**
 * 
 */
package experiment.utils.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.IndexedColors;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.ExcelReader;
import experiment.utils.report.excel.ExcelUtils;
import experiment.utils.report.excel.ExcelWriter;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;

/**
 * @author LLT
 *
 */
public class ComparedReportGenerator {

	private static final int HEADER_ROW_IDX = ExcelUtils.HEADER_ROW_NUM;

	public static void generateReport(String resultFile, List<String> reportsFiles, Map<String, List<String>> keyCols) {
		try {
			File file = new File(resultFile);
			if (file.exists()) {
				FileUtils.backupFile(file.getAbsolutePath());
				file.delete();
			}
			List<ExcelReader> allExeclReader = new ArrayList<>();
			for (String otherReport : reportsFiles) {
				ExcelReader excelReader = new ExcelReader(new File(otherReport), HEADER_ROW_IDX);
				allExeclReader.add(excelReader);
			}
			ExcelWriter excelWriter = new ExcelWriter(new File(resultFile));
			for (String sheetName : allExeclReader.get(0).listSheetNames()) {
				List<String> mergedHeaders = mergeHeaders(allExeclReader.get(0), allExeclReader, sheetName);
				List<Records> allRecords = new ArrayList<>();
				for (ExcelReader reader : allExeclReader) {
					allRecords.add(listRecords(reader, sheetName, keyCols, mergedHeaders));
				}
				writeComparation(allRecords, excelWriter, sheetName,
						mergedHeaders.toArray(new String[mergedHeaders.size()]));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SavRtException(e.getMessage());
		}

	}

	private static List<String> mergeHeaders(ExcelReader oldExcelReader, List<ExcelReader> newExcelReaders,
			String sheetName) {
		List<String> oldHeaders = oldExcelReader.listHeader(sheetName);
		List<String> mergedHeaders = new ArrayList<>(oldHeaders);
		for (ExcelReader newExcelReader : newExcelReaders) {
			List<String> newHeaders = newExcelReader.listHeader(sheetName);
			for (String newHeader : newHeaders) {
				if (!mergedHeaders.contains(newHeader)) {
					mergedHeaders.add(newHeader);
				}
			}
		}
		return mergedHeaders;
	}

	private static void writeComparation(List<Records> allRecordsList, ExcelWriter excelWriter, String sheetName,
			String[] mergedHeaders) throws IOException {
		excelWriter.createSheet(sheetName, mergedHeaders, HEADER_ROW_IDX);
		List<IndexedColors> fontColors = new ArrayList<>();
		int s = 3;
		for (int i = 0; i < allRecordsList.size(); i++) {
			int ord = i + s;
			while (ord > IndexedColors.values().length) {
				ord -= IndexedColors.values().length; 
			}
			fontColors.add(IndexedColors.values()[ord]);
		}
		for (String key : allRecordsList.get(0).getKeys()) {
			int i = 0;
			for (Records records : allRecordsList) {
				Record record = records.getRecord(key);
				if (record != null) {
					List<List<Object>> rows = toRowData(Arrays.asList(record));
					excelWriter.writeSheet(sheetName, rows, null, fontColors.get(i));
				}
				i++;
			}
		}
	}

	private static List<List<Object>> toRowData(List<Record> records) {
		List<List<Object>> rowData = new ArrayList<List<Object>>(records.size());
		for (Record record : records) {
			if (record != null) {
				rowData.add(record.getCellValues());
			}
		}
		return rowData;
	}

	private static Records listRecords(ExcelReader excelReader, String sheetName, Map<String, List<String>> keyCols,
			List<String> mergedHeaders) {
		Records records = new Records(mergedHeaders, getKeyCols(keyCols, sheetName));
		for (List<Object> rowData : excelReader.listData(sheetName, mergedHeaders)) {
			records.addRecord(rowData);
		}
		return records;
	}

	private static List<String> getKeyCols(Map<String, List<String>> keyCols, String sheetName) {
		if (CollectionUtils.isEmpty(keyCols)) {
			return Collections.emptyList();
		}
		return CollectionUtils.nullToEmpty(keyCols.get(sheetName));
	}

}
