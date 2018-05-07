/**
 * 
 */
package experiment.utils.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import experiment.utils.report.excel.ExcelReader;
import experiment.utils.report.excel.ExcelUtils;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.TextFormatUtils;

/**
 * @author LLT
 *
 */
public class CsvReportVerifier {
	
	@Test
	public void verifyCsvTestFolder() throws Exception {
		String folderPath = "E:/lyly/TestResult/csv/test";
		File file = new File(folderPath);
		File[] subFiles = file.listFiles();
		for (File subFile : subFiles) {
			if (subFile.isDirectory()) {
				verify(subFile.getAbsolutePath(), "");
			}
		}
	}
	
	@Test
	public void verifySingleFolder() throws Exception {
//		String folderPath = "E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/apache-common-math-2.2";
//		String folderPath = "E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/common-lang";
//		String folderPath = "E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/commons-cli";
//		String folderPath = "E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/apache-collections-3.2.2";
//		String folderPath = "E:/lyly/TestResult/csv/test";
		String folderPath = "E:/lyly/TestResult/csv/test/merge";
		
		verify(folderPath, "");
	}
	
	public void verify(String folder, String filePrefix) throws Exception {
		System.out.println("Folder: " + folder);
		String[] exportFiles = new String[]{
				"random-false-limit-1",
				"random-false-limit-3",
				"random-false-limit-5",
				"random-true-limit-1",
				"random-true-limit-3",
				"random-true-limit-5"};
		String[] sheets = new String[] {"control", "field", "local_var"};
		List<ExcelReader> excelReaders = new ArrayList<ExcelReader>();
		
		for (String file : exportFiles) {
			String filePath = FileUtils.getFilePath(folder, filePrefix + file) + ".xlsx";
			ExcelReader reader = new ExcelReader(new File(filePath), 1);
			excelReaders.add(reader);
		}
		
		Map<String, Map<String, List<String>>> sheetMap = new HashMap<String, Map<String,List<String>>>();
		Set<String> allBugIds = new HashSet<String>();
		for (String sheet : sheets) {
			Map<String, List<String>> fileMap = new HashMap<String, List<String>>();
			sheetMap.put(sheet, fileMap);
			
			for (ExcelReader reader : excelReaders) {
				List<String> bugIds = reader.listData(sheet, "bug_ID");
				fileMap.put(reader.getName(), bugIds);
				allBugIds.addAll(bugIds);
			}
		}
		
		for (ExcelReader reader : excelReaders) {
			System.out.print(reader.getName() + ":  ");
			Set<String> fileAllBugIds = new HashSet<String>();
			Map<String, int[]> breakers = new HashMap<String, int[]>();
			for (String sheet : sheets) {
				int rows = reader.countRow(sheet);
				System.out.print(sheet + ": " + rows + ",  ");
				fileAllBugIds.addAll(sheetMap.get(sheet).get(reader.getName()));
				List<Object> breakerSuccess = reader.listObjData(sheet, "break to bug");
				int success = 0;
				int fail = 0;
				int total = 0;
				for (Object r : breakerSuccess) {
					if (Boolean.TRUE.equals(r)) {
						success++;
					} else {
						fail++;
					}
					total++;
				}
				breakers.put(sheet, new int[]{total, success, fail});
			}
//			System.out.println("Total bugId: " + fileAllBugIds.size());
//			System.out.println("Missing bugIds: ");
//			System.out.println(TextFormatUtils.printCol(CollectionUtils.subtract(allBugIds, fileAllBugIds), "\n"));
			System.out.println("\nbreak to bug: \n");
			System.out.println("[TOTAL, TRUE, FALSE]");
			System.out.println(TextFormatUtils.printMap(breakers));
		}
		
		for (String sheet : sheetMap.keySet()) {
//			System.out.println("Sheet: " + sheet);
			Map<String, List<String>> bugMap = sheetMap.get(sheet);
			Set<String> sheetBugIds = CollectionUtils.concateToSet(bugMap.values());
			for (String file : bugMap.keySet()) {
				List<String> bugs = bugMap.get(file);
//				System.out.println("Missing for File: " + file);
//				System.out.println(TextFormatUtils.printCol(CollectionUtils.subtract(sheetBugIds, bugs), "\n"));
			}
		}
	}
	
	@Test
	public void mergeExcels() throws IOException {
		List<String> inputFolders = new ArrayList<String>();
		int headerRowNum = 0;
		String folderPath = "E:/lyly/TestResult/csv/test";
		File file = new File(folderPath);
		File[] subFiles = file.listFiles();
		for (File subFile : subFiles) {
			if (subFile.isDirectory() && !"merge".equals(subFile.getName())) {
				inputFolders.add(subFile.getAbsolutePath());
			}
		}
//		List<String> inputFolders = Arrays.asList(
//				"E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/JFreeChart",
////				"E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/common-lang",
//				"E:/lyly/eclipse-java-mars-clean/eclipse/trace-test/mutation/apache-common-math-2.2");
		String outputFolder = "E:/lyly/TestResult/csv/test/merge";
		FileUtils.createFolder(outputFolder);
		String[] exportFiles = new String[]{
				"random-false-limit-1",
				"random-false-limit-3",
				"random-false-limit-5",
				"random-true-limit-1",
				"random-true-limit-3",
				"random-true-limit-5"};
		
		for (String fileName : exportFiles) {
			String outputFile = FileUtils.getFilePath(outputFolder, fileName) + ".xlsx";
			List<String> inputFiles = new ArrayList<String>(inputFolders.size());
			for (String inputFolder : inputFolders) {
				inputFiles.add(FileUtils.getFilePath(inputFolder, fileName + ".xlsx"));
			}
			ExcelUtils.mergeExcel(outputFile, inputFiles, headerRowNum);
		}
		
	}
}
