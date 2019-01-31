package experiment.utils.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.IndexedColors;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.ExcelUtils;
import experiment.utils.report.excel.ExcelWriter;
import experiment.utils.report.rules.IComparisonRule;

public class ExcelReporter implements IReporter {
	private static final int HEADER_ROW_IDX = ExcelUtils.HEADER_ROW_NUM;
	private ExcelWriter excelWriter;
	
	public ExcelReporter(String resultFile) {
		excelWriter = new ExcelWriter(new File(resultFile));
	}

	@Override
	public void writeChanges(ReportChanges reportChanges, String sheetName, String[] mergedHeaders)
			throws IOException {
		excelWriter.createSheet(sheetName, mergedHeaders, HEADER_ROW_IDX);
		List<List<Object>> missingData = toRowData(reportChanges.getMissingRecords());
		excelWriter.writeSheet(sheetName, missingData, null, IndexedColors.RED);
		List<List<Object>> newData = toRowData(reportChanges.getAddedRecords());
		excelWriter.writeSheet(sheetName, newData, null, IndexedColors.GREEN);
		for (IComparisonRule rule : reportChanges.getAllRules()) {
			String comparisonSheetName = sheetName + "_" + rule.getName();
			excelWriter.createSheet(comparisonSheetName, mergedHeaders, HEADER_ROW_IDX);
			excelWriter.writeNewRecord(comparisonSheetName, mergedHeaders, reportChanges.getImproves().get(rule),
					IndexedColors.GREEN);
			excelWriter.writeNewRecord(comparisonSheetName, mergedHeaders, reportChanges.getDeclines().get(rule),
					IndexedColors.ORANGE);
			excelWriter.writeNewRecord(comparisonSheetName, mergedHeaders, reportChanges.getChanges().get(rule),
					IndexedColors.BLUE);
			
			comparisonSheetName = comparisonSheetName + "_diff";
			excelWriter.createSheet(comparisonSheetName, mergedHeaders, HEADER_ROW_IDX);
			excelWriter.writeRecordDiffs(comparisonSheetName, mergedHeaders, 
					reportChanges.getImproves().get(rule), IndexedColors.LIGHT_GREEN, IndexedColors.RED);
			excelWriter.writeRecordDiffs(comparisonSheetName, mergedHeaders, 
					reportChanges.getDeclines().get(rule), IndexedColors.LIGHT_YELLOW, IndexedColors.RED);
			excelWriter.writeRecordDiffs(comparisonSheetName, mergedHeaders,
					reportChanges.getChanges().get(rule), IndexedColors.GREY_25_PERCENT, IndexedColors.RED);
		}
	}
	
	private static List<List<Object>> toRowData(List<Record> records) {
		List<List<Object>> rowData = new ArrayList<List<Object>>(records.size());
		for (Record record : records) {
			rowData.add(record.getCellValues());
		}
		return rowData;
	}
}
