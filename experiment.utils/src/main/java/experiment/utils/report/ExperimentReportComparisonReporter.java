/**
 * 
 */
package experiment.utils.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import experiment.utils.report.excel.ExcelReader;
import experiment.utils.report.excel.ExcelUtils;
import experiment.utils.report.rules.IComparisonRule;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;

/**
 * @author LLT
 *
 */
public class ExperimentReportComparisonReporter {
	private static final int HEADER_ROW_IDX = ExcelUtils.HEADER_ROW_NUM;
	
	public static <T extends IComparisonRule> void reportChange(String resultFile, String oldReport, String newReport,
			List<T> rules, Map<String, List<String>> keyCols) {
		try {
			File file = new File(resultFile);
			if (file.exists()) {
				FileUtils.backupFile(file.getAbsolutePath());
				file.delete();
			}
			ExcelReader oldExcelReader = new ExcelReader(new File(oldReport), HEADER_ROW_IDX);
			ExcelReader newExcelReader = new ExcelReader(new File(newReport), HEADER_ROW_IDX);
			IReporter reporter = getReporter(resultFile);
			for (String sheetName : oldExcelReader.listSheetNames()) {
				List<String> mergedHeaders = mergeHeaders(oldExcelReader, newExcelReader, sheetName);
				Records oldRecords = listRecords(oldExcelReader, sheetName, keyCols, mergedHeaders);
				Records newRecords = listRecords(newExcelReader, sheetName, keyCols, mergedHeaders);
				ReportChanges reportChanges = RecordsComparator.compare(oldRecords, newRecords, rules);
				reporter.writeChanges(reportChanges, sheetName,
						mergedHeaders.toArray(new String[mergedHeaders.size()]));
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new SavRtException(e.getMessage());
		}
	}

	private static IReporter getReporter(String resultFile) {
		if (resultFile.endsWith(".txt")) {
			return new StatisticReporter(resultFile);
		} else if (resultFile.endsWith(".xlsx")) {
			return new ExcelReporter(resultFile);
		}
		throw new IllegalArgumentException("Invalid report file! " + resultFile);
	}

	private static List<String> mergeHeaders(ExcelReader oldExcelReader, ExcelReader newExcelReader, String sheetName) {
		List<String> oldHeaders = oldExcelReader.listHeader(sheetName);
		List<String> newHeaders = newExcelReader.listHeader(sheetName);
		List<String> mergedHeaders = new ArrayList<>(oldHeaders);
		for (String newHeader : newHeaders) {
			if (!mergedHeaders.contains(newHeader)) {
				mergedHeaders.add(newHeader);
			}
		}
		return mergedHeaders;
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
