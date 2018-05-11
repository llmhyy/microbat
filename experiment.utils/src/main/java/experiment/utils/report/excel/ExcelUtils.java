/**
 * 
 */
package experiment.utils.report.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class ExcelUtils {
	public static final int HEADER_ROW_NUM = 0;
	
	public static void mergeExcel(String outputFile, List<String> inputFiles, int headerRowNum) throws IOException {
		if (CollectionUtils.isEmpty(inputFiles)) {
			return;
		}
		List<ExcelReader> excelReaders = new ArrayList<ExcelReader>();
		for (String inputFile : inputFiles) {
			excelReaders.add(new ExcelReader(new File(inputFile), headerRowNum));
		}
		List<String> sheets = collectAllSheetNames(excelReaders);
		ExcelWriter excelWriter = new ExcelWriter(new File(outputFile));
		for (String sheet : sheets) {
			String[] headers = collectHeaders(excelReaders, sheet);
			excelWriter.createSheet(sheet, headers, headerRowNum);
			for (ExcelReader reader : excelReaders) {
				List<List<Object>> listData = reader.listData(sheet);
				excelWriter.writeSheet(sheet, listData);
			}
		}
	}
	
	public static void createSheet(ExcelWriter excelWriter, String sheetName, ExcelReader excelReader,
			int headerRowNum) {
		String[] headers = collectHeaders(excelReader, sheetName);
		excelWriter.createSheet(sheetName, headers, headerRowNum);
	}

	public static String[] collectHeaders(List<ExcelReader> excelReaders, String sheet) {
		List<String> headers = excelReaders.get(0).listHeader(sheet);
		return headers.toArray(new String[headers.size()]);
	}
	
	public static String[] collectHeaders(ExcelReader excelReader, String sheet) {
		List<String> headers = excelReader.listHeader(sheet);
		return headers.toArray(new String[headers.size()]);
	}

	private static List<String> collectAllSheetNames(List<ExcelReader> excelReaders) {
		List<String> sheets = new ArrayList<String>();
		for (ExcelReader reader : excelReaders) {
			CollectionUtils.addIfNotExist(sheets, reader.listSheetNames());
		}
		return sheets;
	}

	public static List<String> collectKeys(String excelFile, String sheetName, List<String> keyCols,
			String separator) {
		ExcelReader reader = new ExcelReader(new File(excelFile), 0);
		List<List<Object>> data = new ArrayList<>();
		int rowNum = -1;
		for (String col : keyCols) {
			List<Object> colData = reader.listObjData(sheetName, col);
			rowNum = colData.size();
			data.add(colData);
		}
		List<String> keys = new ArrayList<>();
		for (int i = 0; i < rowNum; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < data.size(); j++) {
				sb.append(data.get(j).get(i));
				if (j != (data.size() - 1)) {
					sb.append(separator);
				}
			}
			keys.add(sb.toString());
		}
		return keys;
	}

}
